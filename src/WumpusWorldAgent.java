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

import aima.core.environment.wumpusworld.*;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import aima.core.agent.impl.SimpleEnvironmentView;
import aima.core.logic.propositional.inference.DPLLSatisfiable;

import java.util.*;

import java.util.Hashtable;

public class WumpusWorldAgent extends Agent {

	public Hashtable<String, HybridWumpusAgent> world_agents;
	private HybridWumpusAgent agent;
	private WumpusEnvironment world;
	private WumpusCave cave;

	protected void setup() {
		// Creating world
		cave = create4x4Cave();
		world = new WumpusEnvironment(cave);
		SimpleEnvironmentView view = new SimpleEnvironmentView();
        world.addEnvironmentListener(view);


		// Register service in the yellow pages
		ServiceRegistration registrar = new ServiceRegistration(this, "cave-excursion");
		registrar.registerService();
		// Add the behaviour serving queries from buyer agents
		addBehaviour(new OfferExcursionServer());

		// Add the behaviour serving purchase orders from buyer agents
		addBehaviour(new DiveInToTheCave());
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
		System.out.println("Wumpus world "+getAID().getName()+" terminating.");
	}


	private static WumpusCave create4x4Cave() {
        return new WumpusCave(4, 4, ""
                + ". . . P "
                + "W G P . "
                + ". . . . "
                + "S . P . ");
    }

	private class OfferExcursionServer extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// CFP Message received. Process it
				String title = msg.getContent();
				ACLMessage reply = msg.createReply();

				List <aima.core.agent.Agent<?,?>> agents = world.getAgents();
				if (agents.isEmpty()) {
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent("Welcome to the cave");
					System.out.println("Welcome to the cave");
				}
				else {
					// The requested book is NOT available for sale.
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("Cave is a busy at this moment");
					System.out.println("Cave is a busy at this moment");
				}
				myAgent.send(reply);
				removeBehaviour(this);
			}
			else {
				block();
			}
		}

	}  //

	private class DiveInToTheCave extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);
			List <aima.core.agent.Agent<?,?>> agents = world.getAgents();
			if (msg != null && agents.isEmpty()) {
				System.out.println("Ready to start a game");
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.INFORM);
				System.out.println("Game started " + msg.getSender().getName());
				myAgent.send(reply);
				agent = new HybridWumpusAgent
				// agent = new EfficientHybridWumpusAgent
						(cave.getCaveXDimension(), cave.getCaveYDimension(), cave.getStart(), new DPLLSatisfiable(), world);
//				world_agents.put(msg.getSender().toString(), agent);
				world.notify("The cave:\n" + cave.toString());
				world.addAgent(agent);
//				world.stepUntilDone();
				world.notify("Metrics: " + agent.getMetrics());
//				world.notify("KB:\n" + agent.getKB());
				addBehaviour(new StatusWaiter());
				removeBehaviour(this);
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
			MessageTemplate cfp_mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage cfp = myAgent.receive(cfp_mt);
			if (msg != null) {
				System.out.println("Request for conditions");
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.INFORM);
				System.out.println("Replying with: " + world.getPerceptSeenBy(agent));
				reply.setContent(world.getPerceptSeenBy(agent).toString());
				myAgent.send(reply);
			} else if (cfp != null) {
				String action = cfp.getContent();
				System.out.println("Call to action: " + action);
				world.execute(agent, WumpusAction.valueOf(action));
				if ("CLIMB".equals(action)) {
					world.notify("Metrics: " + agent.getMetrics());
					world.notify("KB:\n" + agent.getKB());
					myAgent.doDelete();
				} else {
					ACLMessage reply = cfp.createReply();
					reply.setPerformative(ACLMessage.INFORM);
					System.out.println("Replying with: " + world.getPerceptSeenBy(agent));
					reply.setContent(world.getPerceptSeenBy(agent).toString());
					myAgent.send(reply);
				}
			} else {
				block();
			}
		}
	}
}
