package org.apache.james.transport.mailets;


import jakarta.inject.Inject;

import org.apache.james.dnsservice.api.DNSService;
import org.apache.james.domainlist.api.DomainList;
import org.apache.james.metrics.api.MetricFactory;
import org.apache.james.queue.api.MailQueueFactory;


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

    @Override public String getMailetInfo() {
        return "JWM RemoteDelivery Mailet";
    }
}
