package operations;

import datatypes.Project;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;

@Operation(name="Proxy",description="Set the proxy server.")
public class SetProxy {
	private Boolean useProxy;
	private String host="none";
	private Integer port=0;
	private String username="none";
	private String password="none";
	private Project project;
	private Boolean useAuthentication;
	
	
	/**
	 * @param host the host to set
	 */
	@Port(direction=Direction.INPUT, name="Select Project",validateMethod="checkProject", order=1)
	public void getProject(Project project) {
	
		this.project = project;
	}

	/**
	 * @param project
	 */
	public void checkProject(Project project) {
		
		if(project == null) {
		
			throw new IllegalArgumentException("No Project Selected!");
		}
		else {
			
			this.project = project;
		}
	}
	
	/**
	 * @param host the host to set
	 */
	@Port(direction=Direction.INPUT, name="Use Proxy?",defaultValue="false",validateMethod="checkUseProxy", order=2)
	public void useProxy(boolean useProxy) {
		
		this.useProxy = useProxy;
	}
	
	/**
	 * @param useProxy
	 */
	public void checkUseProxy(boolean useProxy){
		
		this.useProxy = useProxy;
		this.project.setUseProxy(useProxy);
	}

	/**
	 * @param host the host to set
	 */
	@Port(direction=Direction.INPUT, name="HTTP Proxy Host",validateMethod="checkHost", order=3)
	public void setHost(String host) {
		
		this.host = host;
		if(!host.startsWith("http://")) {
			
			this.host = "http://".concat(host);
		}
	}
	
	/**
	 * @param host
	 */
	public void checkHost(String host) {
		
		
		if(this.useProxy 
				&& (host.isEmpty() ||
						!(host instanceof String))) {
			
			throw new IllegalArgumentException("Please Insert Host Name");
		}
		
		else if(this.useProxy) {
				
			this.host = host;
			this.project.setProxy_host(this.host);
			
			System.setProperty("http.proxyHost", this.host);
		}
	}

	/**
	 * @param port the port to set
	 */
	@Port(direction=Direction.INPUT, name="Proxy Port", validateMethod="checkPort",order=4)
	public void setPort(Integer port) {
		
		this.port = port;
	}
	
	/**
	 * @param port
	 */
	public void checkPort(Integer port) {
		
		if(this.useProxy && ((port+"").isEmpty() || !(port instanceof Integer))) {
			
			throw new IllegalArgumentException("Please Insert a number");
		}
		else if(this.useProxy) {
			
			this.port = port;	
			this.project.setProxy_port(this.port.toString());
			
			System.setProperty("http.proxyPort", "port_number");
		}
	}

	/**
	 * @param host the host to set
	 */
	@Port(direction=Direction.INPUT, name="Proxy Requires Authentication?", order=5)
	public void useAuthentication(boolean useAuthentication) {
		
		this.useAuthentication = useAuthentication;
		this.project.setUseAuthentication(useAuthentication);
	}
	
	/**
	 * @param username the username to set
	 */
	@Port(direction=Direction.INPUT, name="Proxy User Name", description="(Optional)", order=6)
	public void setUsername(String username) {
		
		this.project.setProxy_username(this.username);
		this.username = username;
		System.setProperty("http.proxyUserName", this.username);
	}

	/**
	 * @param password the password to set
	 */
	@Port(direction=Direction.INPUT, name="Proxy Password", description="(Optional)", order=7)
	public void setPassword(String password) {
		
		this.password = password;

		if(this.useProxy) {
			
			this.project.setProxy_host(this.host);
			this.project.setProxy_port(this.port.toString());
			this.project.setUseAuthentication(useAuthentication);
			if(this.useAuthentication){this.project.setProxy_password(this.password);}
			if(this.useAuthentication){this.project.setProxy_username(this.username);}
			System.setProperty("http.proxyPassword", this.password);
		}
//		else
//		{
//			this.project.setProxy_host("none");
//			this.project.setProxy_port("80");
//			this.project.setProxy_password("none");
//			this.project.setProxy_username("none");
//			this.project.setUseAuthentication(false);
//		}
	}	

}
