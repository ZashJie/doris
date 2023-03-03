// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.doris.service;

import org.apache.doris.common.CIDR;
import org.apache.doris.common.Config;
import org.apache.doris.common.util.NetUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Inet4Address;
// import java.net.Inet6Address;  // add
import java.net.InetAddress;
// import java.net.InterfaceAddress; // add
import java.net.NetworkInterface;  // add
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections; //ad
import java.util.Enumeration; //add
import java.util.List;


public class FrontendOptions {
    private static final Logger LOG = LogManager.getLogger(FrontendOptions.class);

    private static String PRIORITY_CIDR_SEPARATOR = ";";

    private static List<InetAddress> netInterfaces = Lists.newArrayList();
    private static List<CIDR> priorityCidrs = Lists.newArrayList();
    private static InetAddress localAddr = InetAddress.getLoopbackAddress();

    public static void init() throws UnknownHostException {
        localAddr = null;
        if (!"0.0.0.0".equals(Config.frontend_address)) {
            if (!InetAddressValidator.getInstance().isValidInet4Address(Config.frontend_address)) {
                throw new UnknownHostException("invalid frontend_address: " + Config.frontend_address);
            }
            localAddr = InetAddress.getByName(Config.frontend_address);
            LOG.info("use configured address. {}", localAddr);
            return;
        }

        try {
            analyzeNetworkInterfaces();
        } catch (Exception e) {
            LOG.info("Failed to analyze the netInterface");
        }

        analyzePriorityCidrs();

        // if not set frontend_address, get a non-loopback ip
        List<InetAddress> hosts = new ArrayList<>();
        NetUtils.getHosts(hosts);
        if (hosts.isEmpty()) {
            LOG.error("fail to get localhost");
            System.exit(-1);
        }

        InetAddress loopBack = null;
        for (InetAddress addr : hosts) {
            LOG.debug("check ip address: {}", addr);
            if (addr instanceof Inet4Address) {
                if (addr.isLoopbackAddress()) {
                    loopBack = addr;
                } else if (!netInterfaces.isEmpty()) {
                    if (isInNetInterface(addr.getHostAddress())) {
                        localAddr = addr;
                        break;
                    }
                } else if (!priorityCidrs.isEmpty()) {
                    if (isInPriorNetwork(addr.getHostAddress())) {
                        localAddr = addr;
                        break;
                    }
                } else {
                    localAddr = addr;
                    break;
                }
            }
        }

        // nothing found, use loopback addr
        if (localAddr == null) {
            localAddr = loopBack;
        }
        LOG.info("local address: {}.", localAddr);
    }

    public static InetAddress getLocalHost() {
        return localAddr;
    }

    public static String getLocalHostAddress() {
        return localAddr.getHostAddress();
    }

    public static String getHostname() {
        return localAddr.getHostName();
    }

    private static void analyzePriorityCidrs() {
        String priorCidrs = Config.priority_networks;
        if (Strings.isNullOrEmpty(priorCidrs)) {
            return;
        }
        LOG.info("configured prior_cidrs value: {}", priorCidrs);

        String[] cidrList = priorCidrs.split(PRIORITY_CIDR_SEPARATOR);
        List<String> priorNetworks = Lists.newArrayList(cidrList);
        for (String cidrStr : priorNetworks) {
            priorityCidrs.add(new CIDR(cidrStr));
        }
    }

    private static void analyzeNetworkInterfaces() throws Exception {
        String nicNames = Config.network_interfaces;
        if (Strings.isNullOrEmpty(nicNames)) {
            return;
        }
        LOG.info("configured Network Interfaces value: {}", nicNames);

        String[] nicList = nicNames.split(PRIORITY_CIDR_SEPARATOR);
        List<String> netinterLists = Lists.newArrayList(nicList);
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();

        for (String nicStr : netinterLists) {
            for (NetworkInterface netint : Collections.list(nets)) {
                Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                if (nicStr.equals(netint.getName())) {
                    for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                        if (inetAddress instanceof Inet4Address) {
                            netInterfaces.add(inetAddress);
                        }
                    }
                }
            }
        }
    }

    private static boolean isInPriorNetwork(String ip) {
        for (CIDR cidr : priorityCidrs) {
            if (cidr.contains(ip)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isInNetInterface(String ip) {
        for (InetAddress netaddr : netInterfaces) {
            if (ip.equals(netaddr.getHostAddress())) {
                return true;
            }
        }
        return false;
    }
}
