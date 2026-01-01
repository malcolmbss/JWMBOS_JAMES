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

public class JWMRemoteDelivery extends RemoteDelivery
{
    @Inject
    public JWMRemoteDelivery(DNSService dnsServer, DomainList domainList, MailQueueFactory<?> queueFactory, MetricFactory metricFactory) {
        super(dnsServer, domainList, queueFactory, metricFactory);
    }

    public JWMRemoteDelivery(DNSService dnsServer, DomainList domainList, MailQueueFactory<?> queueFactory, MetricFactory metricFactory, ThreadState startThreads) {
        super(dnsServer, domainList, queueFactory, metricFactory, startThreads);
    }

    public String getMailetInfo() {
        return "JWM RemoteDelivery Mailet";
    }
}
