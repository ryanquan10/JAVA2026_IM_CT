
package org.tio.sitexxx.service.service;

import java.util.List;

import org.redisson.connection.ClientConnectionsEntry;
import org.redisson.connection.balancer.RandomLoadBalancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyRandomLoadBalancer extends RandomLoadBalancer {
	private static Logger log = LoggerFactory.getLogger(MyRandomLoadBalancer.class);

	/* 
	 * 
	 * @see org.redisson.connection.balancer.RandomLoadBalancer#getEntry(java.util.List)
	 * 
	 */
	@Override
	public ClientConnectionsEntry getEntry(List<ClientConnectionsEntry> clientsCopy) {
		ClientConnectionsEntry test = super.getEntry(clientsCopy);
		log.info(test.getClient().getAddr().getHostString() + ":" + test.getClient().getAddr().getPort());
		return test;
	}

}
