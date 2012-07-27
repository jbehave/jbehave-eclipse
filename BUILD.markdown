# Requirements

Building the JBehave Eclipse requires:

* Eclipse SDK 3.7 (Indigo) or 4.2 (Juno) or above.
* Java 1.6 or above
* Maven 3.0 or above 

# To build

* Download external dependencies to the lib/ directory via Maven:
 
  ./download-lib.sh
  
* Build Eclipse p2 repository using Maven:

  mvn clean install 
  
  Built artifact in org.jbehave.eclipse.repository/target/org.jbehave.eclipse.repository-x.y.z-SNAPSHOT.zip can be used by Eclipse Installer 

* Upload updates site:

  ./upload-eclipse.sh
     
   The repository will be available as Updates site on http://jbehave.org/reference/eclipse/updates/  
     
# To launch the plugin in an runtime workspace

* Righ click on `Eclipse Workbench.launch` Run as...
