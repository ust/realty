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
	<h2>Here's the plan:</h2>
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
	</ol>
</body>
</html>