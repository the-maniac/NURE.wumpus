import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class ServiceRegistration {
    Agent agent;
    String service_name;
    AID[] services;

    public ServiceRegistration(Agent agent, String service_name) {
        this.agent = agent;
        this.service_name = service_name;
    }

    public void registerService() {
        DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(this.agent.getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType(service_name);
		sd.setName(service_name);
		dfd.addServices(sd);
		try {
			DFService.register(this.agent, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		services = new AID[1];
		services[0] = this.agent.getAID();
    }

    public void findService() {
        System.out.println("Looking for the service " + service_name);
        // Update the list of seller agents
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(service_name);
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this.agent, template);
            if (result.length > 0){
                System.out.println("Found Service:");
            } else {
                System.out.println("Service " + service_name + " not found");
            }

            services = new AID[result.length];
            for (int i = 0; i < result.length; ++i) {
                services[i] = result[i].getName();
                System.out.println(services[i].getName());
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    public AID[] getServices() {
        return services;
    }
}
