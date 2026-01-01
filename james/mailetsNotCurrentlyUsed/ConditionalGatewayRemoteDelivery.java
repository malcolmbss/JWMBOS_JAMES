/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.transport.mailets;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;
import javax.mail.MessagingException;

import org.apache.james.core.Domain;
import org.apache.james.core.MailAddress;
import org.apache.james.dnsservice.api.DNSService;
import org.apache.james.domainlist.api.DomainList;
import org.apache.james.metrics.api.MetricFactory;
import org.apache.james.queue.api.MailPrioritySupport;
import org.apache.james.queue.api.MailQueue;
import org.apache.james.queue.api.MailQueue.MailQueueException;
import org.apache.james.queue.api.MailQueueFactory;
import org.apache.james.transport.mailets.remote.delivery.Bouncer;
import org.apache.james.transport.mailets.remote.delivery.DeliveryRunnable;
import org.apache.james.transport.mailets.remote.delivery.RemoteDeliveryConfiguration;
import org.apache.james.transport.mailets.remote.delivery.RemoteDeliverySocketFactory;
import org.apache.mailet.Mail;
import org.apache.mailet.base.GenericMailet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;

public class ConditionalGatewayRemoteDelivery extends GenericMailet {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConditionalGatewayRemoteDelivery.class);
    private DeliveryRunnable deliveryRunnable;

    public enum ThreadState {
        START_THREADS,
        DO_NOT_START_THREADS
    }

    public static final String NAME_JUNCTION = "-to-";
    private String[] gatewayExemptionDomains = null;
    private String[] gatewayExemptionSenders = null;

    private final DNSService dnsServer;
    private final DomainList domainList;
    private final MailQueueFactory<?> queueFactory;
    private final MetricFactory metricFactory;
    private final ThreadState startThreads;

    private MailQueue queue;
    private RemoteDeliveryConfiguration configuration;

    @Inject
    public ConditionalGatewayRemoteDelivery(DNSService dnsServer, DomainList domainList, MailQueueFactory<?> queueFactory, MetricFactory metricFactory) {
        this(dnsServer, domainList, queueFactory, metricFactory, ThreadState.START_THREADS);
    }

    public ConditionalGatewayRemoteDelivery(DNSService dnsServer, DomainList domainList, MailQueueFactory<?> queueFactory, MetricFactory metricFactory, ThreadState startThreads) {
        this.dnsServer = dnsServer;
        this.domainList = domainList;
        this.queueFactory = queueFactory;
        this.metricFactory = metricFactory;
        this.startThreads = startThreads;
    }

    @Override
    public void init() throws MessagingException {
        configuration = new RemoteDeliveryConfiguration(getMailetConfig(), domainList);
        gatewayExemptionDomains = getInitParameter("gatewayExemptionDomains").toLowerCase().split(";");
        gatewayExemptionSenders = getInitParameter("gatewayExemptionSenders").toLowerCase().split(";");
        queue = queueFactory.createQueue(configuration.getOutGoingQueueName());
        try {
            if (configuration.isBindUsed()) {
                RemoteDeliverySocketFactory.setBindAdress(configuration.getBindAddress());
            }
        } catch (UnknownHostException e) {
            LOGGER.error("Invalid bind setting ({}): ", configuration.getBindAddress(), e);
        }
        deliveryRunnable = new DeliveryRunnable(queue,
            configuration,
            dnsServer,
            metricFactory,
            getMailetContext(),
            new Bouncer(configuration, getMailetContext()));
        if (startThreads == ThreadState.START_THREADS) {
            deliveryRunnable.start();
        }
    }

    @Override
    public String getMailetInfo() {
        return "RemoteDelivery Mailet";
    }

    @Override
    public void service(Mail mail) throws MessagingException {
        if (configuration.isDebug()) {
            LOGGER.debug("Remotely delivering mail {}", mail.getName());
        }
        if (configuration.isUsePriority()) {
            mail.setAttribute(MailPrioritySupport.HIGH_PRIORITY_ATTRIBUTE);
        }
        if (!mail.getRecipients().isEmpty()) {
            if (configuration.getGatewayServer().isEmpty()) {
                serviceNoGateway(mail);
            } else {
                serviceWithGateway(mail);
            }
        } else {
            LOGGER.debug("Mail {} from {} has no recipients and can not be remotely delivered", mail.getName(), mail.getMaybeSender());
        }
        mail.setState(Mail.GHOST);
    }

    protected void serviceWithGateway(Mail mail) {
        String senderHost = (mail.getSender().getDomain().name()).toLowerCase();
        String senderAddress = mail.getSender().toString().toLowerCase();
        boolean useGateway = true;
        for (int i=0; i < gatewayExemptionDomains.length; i++ )
        {
           LOGGER.debug( i + " ["+ gatewayExemptionDomains[i] + "] ?= [" + senderHost + "]" );
           if ( gatewayExemptionDomains[i].equals( senderHost ) ) useGateway = false;
        }
        for (int i=0; i < gatewayExemptionSenders.length; i++ )
        {
           LOGGER.debug( i + " ["+ gatewayExemptionSenders[i] + "] ?= [" + senderAddress + "]" );
           if ( gatewayExemptionSenders[i].equals( senderAddress ) ) useGateway = false;
        }
        try {
            if ( useGateway )
            {
               LOGGER.debug("Sending mail using gateway to {}", mail.getRecipients());
               queue.enQueue(mail);
            }
            else
            {
               LOGGER.debug("Sending mail NOT using gateway to {}", mail.getRecipients());
               serviceNoGateway( mail );
            }
        } catch (MailQueueException e) {
            LOGGER.error("Unable to queue mail {} for recipients {}", mail.getName(), mail.getRecipients(), e);
        }
    }

    protected void serviceNoGateway(Mail mail) {
        String mailName = mail.getName();
        Map<Domain, Collection<MailAddress>> targets = groupByServer(mail.getRecipients());
        for (Map.Entry<Domain, Collection<MailAddress>> entry : targets.entrySet()) {
            serviceSingleServer(mail, mailName, entry);
        }
    }

    protected void serviceSingleServer(Mail mail, String originalName, Map.Entry<Domain, Collection<MailAddress>> entry) {
        if (configuration.isDebug()) {
            LOGGER.debug("Sending mail to {} on host {}", entry.getValue(), entry.getKey());
        }
        mail.setRecipients(entry.getValue());
        mail.setName(originalName + NAME_JUNCTION + entry.getKey().name());
        try {
            queue.enQueue(mail);
        } catch (MailQueueException e) {
            LOGGER.error("Unable to queue mail {} for recipients {}", mail.getName(), mail.getRecipients(), e);
        }
    }

    protected Map<Domain, Collection<MailAddress>> groupByServer(Collection<MailAddress> recipients) {
        // Must first organize the recipients into distinct servers (name made case insensitive)
        HashMultimap<Domain, MailAddress> groupByServerMultimap = HashMultimap.create();
        for (MailAddress recipient : recipients) {
            groupByServerMultimap.put(recipient.getDomain(), recipient);
        }
        return groupByServerMultimap.asMap();
    }

    /**
     * Stops all the worker threads that are waiting for messages. This method
     * is called by the Mailet container before taking this Mailet out of
     * service.
     */
    @Override
    public synchronized void destroy() {
        if (startThreads == ThreadState.START_THREADS) {
            deliveryRunnable.dispose();
        }
    }
}
