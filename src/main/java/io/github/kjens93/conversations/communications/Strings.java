package io.github.kjens93.conversations.communications;

/**
 * Created by kjensen on 12/13/16.
 */
interface Strings {

    String sent_udp = "Sent [%s] via [UDP] to [%s]";
    String rcvd_udp = "Received [%s] via [UDP] from [%s]";
    String opnd_udp = "Opened UDP socket on port [%d]";
    String clsd_udp = "Closed UDP socket on port [%d]";
    String opnd_tcp = "Opened TCP connection socket";
    String clsd_tcp = "Closed TCP connection socket on port [%d]";
    String bond_tcp = "Bound TCP connection socket on port [%d] to remote [%s]";
    String opnd_tcs = "Opened TCP server socket on port [%d]";
    String clsd_tcs = "Closed TCP server socket on port [%d]";

}
