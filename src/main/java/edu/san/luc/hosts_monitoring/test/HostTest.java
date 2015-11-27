package edu.san.luc.hosts_monitoring.test;

import java.net.URL;

/**
 * Created by sanya on 13.11.15.
 */
public interface HostTest {
    Integer test() throws Exception;

    URL getURL();
}
