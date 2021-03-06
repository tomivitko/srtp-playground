package de.conet.srtp.playground.wsclient;

import gov.nist.javax.sdp.MediaDescriptionImpl;
import org.ice4j.Transport;
import org.ice4j.TransportAddress;
import org.ice4j.ice.Agent;
import org.ice4j.ice.IceMediaStream;
import org.ice4j.ice.harvest.StunCandidateHarvester;
import org.ice4j.ice.harvest.TurnCandidateHarvester;
import org.ice4j.ice.harvest.UPNPHarvester;
import org.ice4j.security.LongTermCredential;

public class AgentUtils {


    protected static Agent createAgent(int rtpPort, boolean isTrickling, final MediaDescriptionImpl mediaDescription) throws Throwable {
        Agent agent = new Agent();
        agent.setTrickling(isTrickling);

        // STUN
        StunCandidateHarvester stunHarv = new StunCandidateHarvester(
            new TransportAddress("stun.l.google.com", 19302, Transport.UDP));
        //            StunCandidateHarvester stun6Harv = new StunCandidateHarvester(
        //                new TransportAddress("stun6.jitsi.net", 3478, Transport.UDP));

        agent.addCandidateHarvester(stunHarv);
        //            agent.addCandidateHarvester(stun6Harv);

        // TURN
//        String[] hostnames = new String[]
//            {
//                "turn.bistri.com"
//            };
//        int port = 80;
//        LongTermCredential longTermCredential
//            = new LongTermCredential("homeo", "homeo!!");
//
//        for (String hostname : hostnames)
//            agent.addCandidateHarvester(
//                new TurnCandidateHarvester(
//                    new TransportAddress(
//                        hostname, port, Transport.UDP),
//                    longTermCredential));

        //UPnP: adding an UPnP harvester because they are generally slow
        //which makes it more convenient to test things like trickle.
        agent.addCandidateHarvester(new UPNPHarvester());

        //STREAMS
        createStream(rtpPort, "application", agent, mediaDescription);

        return agent;
    }

    public static IceMediaStream createStream(int rtpPort, String streamName, Agent agent, final MediaDescriptionImpl mediaDescription)
        throws Throwable {
        IceMediaStream stream = agent.createMediaStream(streamName);

        //TODO: component creation should probably be part of the library. it
        //should also be started after we've defined all components to be
        //created so that we could run the harvesting for everyone of them
        //simultaneously with the others.

        stream.setMediaDescription(mediaDescription);
        stream.setRemotePassword(mediaDescription.getAttribute("ice-pwd"));
        stream.setRemoteUfrag(mediaDescription.getAttribute("ice-ufrag"));

        //rtp
        agent.createComponent(stream, rtpPort, rtpPort, rtpPort + 100);

        //rtcpComp
        agent.createComponent(stream, rtpPort + 1, rtpPort + 1, rtpPort + 101);

        return stream;
    }
}
