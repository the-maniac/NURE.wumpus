/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
 *****************************************************************/

import aima.core.environment.wumpusworld.HybridWumpusAgent;
import aima.core.environment.wumpusworld.WumpusAction;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Hashtable;
import java.util.Optional;


public class CaverAgent extends Agent {
	private AID[] worlds;
	private AID[] navigators;
	private AID currentWorld;
	private AID currentNavigator;
	private Hashtable<String, AID> services;

	protected void setup() {

		System.out.println("Caverman "+getAID().getName()+" is ready.");
		services = new Hashtable();
		addBehaviour(new TickerBehaviour(this, 5000) {
			protected void onTick() {
				currentWorld = services.get("cave-excursion");
				currentNavigator = services.get("navigation");
				if (currentWorld != null && currentNavigator != null) {
//				if (currentWorld != null) {
					addBehaviour(new StatusWaiter());
					ACLMessage order = new ACLMessage(ACLMessage.REQUEST);
					order.addReceiver(currentWorld);
					order.setContent("Give me info!");
//					order.setConversationId(conversation_id);
//					order.setReplyWith("order" + System.currentTimeMillis());
					myAgent.send(order);
					this.stop();
					removeBehaviour(this);
				}

				ServiceRegistration caveExcursion = new ServiceRegistration(myAgent, "cave-excursion");
				ServiceRegistration navigation = new ServiceRegistration(myAgent, "navigation");

				if (currentWorld == null) {
					caveExcursion.findService();
					worlds = caveExcursion.getServices();
					PerformRegistration pr = new PerformRegistration(services, worlds, "looking-for-excursion", "cave-excursion");
					myAgent.addBehaviour(pr);
				}

				if (currentNavigator == null) {
					navigation.findService();
					navigators = navigation.getServices();
					PerformRegistration pr = new PerformRegistration(services, navigators, "locking-for-navigation", "navigation");
					myAgent.addBehaviour(pr);
				}
			}
		});
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Bye bye "+getAID().getName()+".");
	}

	private class StatusWaiter extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			MessageTemplate cfp_mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage cfp = myAgent.receive(cfp_mt);
			if (msg != null) {
				String content = msg.getContent();
				NavigatorMessage nm = new NavigatorMessage(content);
				System.out.println("Content: " + nm);
				ACLMessage order = new ACLMessage(ACLMessage.REQUEST);
				order.addReceiver(currentNavigator);
				order.setContent(nm.toString());
				myAgent.send(order);
			} else if (cfp != null) {
				ACLMessage order = new ACLMessage(ACLMessage.CFP);
				order.addReceiver(currentWorld);
				order.setContent(cfp.getContent());
				myAgent.send(order);
				System.out.println("Navigator proposed action: " + cfp.getContent());
				if ("CLIMB".equals(cfp.getContent())) {
					myAgent.doDelete();
				}
			} else {
				block();
			}
		}
	}
}
