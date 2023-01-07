package netGameServer;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName ("Net Game Server Test Suite")
@SelectPackages ({ "netGameServer.primaryTests", "netGameServer.utilitiesTests" })

public class NetGameServerTestSuite {

}
