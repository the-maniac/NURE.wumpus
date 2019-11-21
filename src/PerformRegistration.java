import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Hashtable;

public class PerformRegistration extends Behaviour {
    String conversation_id;
    String service_name;
    AID[] services;
    AID service;
    Hashtable mapping;
//    SuperAgent agent;
    private int repliesCnt = 0; // The counter of replies from seller agents
    private MessageTemplate mt; // The template to receive replies
    private int step = 0;

    public PerformRegistration(Hashtable<String, AID> mapping, AID[] services, String conversation_id, String service_name) {
        super();
        this.conversation_id = conversation_id;
        this.service_name = service_name;
        this.services = services;
        this.mapping = mapping;
    }
    public AID getService() {
        return service;
    }

    public void action() {
        switch (step) {
            case 0:
                // Send the cfp to all sellers
                ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                for (int i = 0; i < services.length; ++i) {
                    cfp.addReceiver(services[i]);
                }
                cfp.setContent("Looking for " + conversation_id);
                cfp.setConversationId(conversation_id);
                cfp.setReplyWith("cfp" + System.currentTimeMillis());
                myAgent.send(cfp);
                mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conversation_id),
                        MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                step = 1;
                break;
            case 1:
                // Receive all proposals/refusals
                ACLMessage reply = myAgent.receive(mt);
                if (reply != null) {
                    if (reply.getPerformative() == ACLMessage.PROPOSE) {
                        if (service == null) {
                            service = reply.getSender();
                            mapping.put(service_name, service);
                        }
                    }
                    repliesCnt++;
                    if (repliesCnt >= services.length) {
                        // We received all replies
                        step = 2;
                    }
                } else {
                    block();
                }
                break;
            case 2:
                // Send the purchase order to the seller that provided the best offer
                ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                order.addReceiver(service);
                order.setContent(conversation_id);
                order.setConversationId(conversation_id);
                order.setReplyWith("order" + System.currentTimeMillis());
                myAgent.send(order);
                // Prepare the template to get the purchase order reply
                mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conversation_id),
                        MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                step = 3;
                break;
            case 3:
                // Receive the purchase order reply
                reply = myAgent.receive(mt);
                if (reply != null) {
                    // Purchase order reply received
                    if (reply.getPerformative() == ACLMessage.INFORM) {
                        // Purchase successful. We can terminate
                        System.out.println(" Service: " + service.getName() + " successfully registered " + reply.getSender().getName());
//                        myAgent.doDelete();
                    } else {
                        System.out.println("Looks like it busy at this moment.");
                    }

                    step = 4;
                } else {
                    block();
                }
                break;
        }
    }

    public boolean done() {
        if (step == 2 && service == null) {
            System.out.println("No service founded");
        }
        return ((step == 2 && service == null) || step == 4);
    }
}
