<html>
<head>
<title>Realty - project description</title>
<!-- If you see it, you are on right way;) -->
</head>
<body>
	<h1>Realty</h1>
	<p>a little bit nostalgy to mmg..</p>
	<h2>Project description</h2>
	<p>helps to find apartments without fucking brokers and earns a many from ad</p>
	<p>v0.2: Little kind of help, actually it only will show is the phone number 
		of real-estate brokers or not</p>
	<h2>"EvoOne" Here's the new plan:</h2>
	<ol>
		<li>Domain investigation</li>
		<li>Service implementation
			<ol>
				<li>Main page with ability to search number 
					and requesting to add new</li>
				<li>Server side solution stores brokers contact info
					and services page requests</li>
				<li>Parser for some donar-site-source, can be ran by hands</li>
			</ol>
		</li>
		<li>Hosting investigation</li>
		<li>Ad research</li>
	</ol>
	<h2><i>DEPRICATED</i> Here's the plan:</h2>
	<ol>
		<li>Design markups</li>
		<li>Write simple page with list of available apartments</li>
		<li>Some kind of web-services (maybe) with a WebSocket interface</li>
		<li>Simple scema on MySQL community server</li>
		<li>Host issue: it can be Google app-engine or some ua VDS</li>
		<li>Domain issue: need in investigation the free-of-charge way of getting org.ua or smt zones</li>
	</ol>
	<h2>Installation</h2>
	<ol>
		<li>Be sure u have java installed, and JAVA_HOME env variable is set properly</li>
		<li>Install Tomcat 7 and running localhost on default port 8080. You have to have manager-gui and manager-script roles specified in CATAINA_HOME/config/tomcat-users.xml</li>
		<li>to run build you have to have maven installed (e.g. external or eclipse+m2plugin) in M2_HOME/conf/setting.xml (or in USER_HOME/.m2/conf/settings.xml)</li>
		<li>Finally you can deploy app by calling "mvn package tomcat7:deploy" command switch. (after chenges tomcat7:redeploy)</li>
		<li>If you want to omit copying resources to the CATALINA_HOME/webapps, you can copy realty.xml context file to the CATALINA_HOME/conf/Catalina/localhost. It specifies BASE_DIR/src/main/webapp as root context. Be sure you provide lib, classes and other resources in to it</li>
	</ol>
</body>
</html>