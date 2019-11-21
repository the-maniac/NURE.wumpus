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
import aima.core.environment.wumpusworld.WumpusCave;
import aima.core.logic.propositional.inference.DPLLSatisfiable;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.List;
import java.util.Optional;

public class NavigationAgent extends Agent {
	HybridWumpusAgent navigator;

	protected void setup() {

		ServiceRegistration registrar = new ServiceRegistration(this, "navigation");
		registrar.registerService();

		addBehaviour(new OfferNavigation());

		// Add the behaviour serving purchase orders from buyer agents
		addBehaviour(new AcceptNavigation());
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Deregister from the yellow pages
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		// Printout a dismissal message
		System.out.println("Navigation "+getAID().getName()+" terminating.");
	}

	private class OfferNavigation extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// CFP Message received. Process it
				String title = msg.getContent();
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.PROPOSE);
				reply.setContent("Free for navigation");
				System.out.println("Free for navigation");
				myAgent.send(reply);
			}
			else {
				block();
			}
		}

	}  //

	private class AcceptNavigation extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				System.out.println("Ready to start navigation");
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.INFORM);
				System.out.println("Navigation started " + msg.getSender().getName());
				myAgent.send(reply);
				addBehaviour(new StatusWaiter());
			}
			else {
				block();
			}
		}
	}
	private class StatusWaiter extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = myAgent.receive(mt);

			if (msg != null) {
				System.out.println("Request for navigation");
				String content = msg.getContent();
				NavigatorMessage nm = new NavigatorMessage(content);
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.CFP);
				if (navigator == null) {
					navigator = new HybridWumpusAgent();
				}
				Optional<WumpusAction> action = navigator.act(nm);

				System.out.println("Action " + action);
				WumpusAction a = action.get();
				reply.setContent(a.toString());
				myAgent.send(reply);
				if ("CLIMB" == a.toString()) {
					myAgent.doDelete();
				}
			} else {
				block();
			}
		}
	}
}
