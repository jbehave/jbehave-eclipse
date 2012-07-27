JBehave Eclipse 
===============

Requirements
------------
Eclipse (Indigo or Juno)

Features of JBehave Eclipse Plugin: 

* Custom Story Parser
* Story Editor

Story Editor:

* Story syntax highlighting
* Step hyperlink detector and implementation jump
* Basic step auto-completion
* Step validation:
  * Detects unimplemented steps, ie invalid step syntax
  * Detects ambiguous steps, ie entry that is match by several implementation

Preference page:

* Story syntax coloring settings
* Localized Keyword support
* Console (and logger level) settings

Installation
============

Use Eclipse Installer 

Updates Site
------------
http://jbehave.org/reference/eclipse/updates/

Local Build
-----------
Build using instructions in BUILD.markdown 

Built repository found in org.jbehave.eclipse.repository/target/repository

RELEASE NOTES
=============

Inspiration
===========

* [GivWenZen](https://bitbucket.org/szczepiq/givwenzenclipse/wiki/Home)
* [Building an Eclipse Text Editor with JFace Text](http://www.realsolve.co.uk/site/tech/jface-text.php)
* [Eclipse Plug-ins, Third Edition](http://www.amazon.com/Eclipse-Plug-ins-3rd-Eric-Clayberg/dp/0321553462/ref=sr_1_1?ie=UTF8&s=books&qid=1300059405&sr=8-1)
* [Erlang IDE ](https://github.com/erlide/erlide)
