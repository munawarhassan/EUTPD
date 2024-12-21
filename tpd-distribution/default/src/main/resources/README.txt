${application.title} Release ${project.version}
======================

About
----------------------

${application.description}


Quick Installation
----------------------

This document describes simply how install quickly. For more information see the Installation Guide documentation to url
${site.base.url}/doc/installation-guide.html.

Requirements:
* JRE 1.8+ (Java)



### Linux and Mac

1. Edit `<${application.title} installation directory>/bin/setenv.sh`

2. Set `${app.home.property}` by uncommenting the `${app.home.property}` line and adding the
   absolute path to the directory where you want ${application.title} to store your data.
   This path MUST NOT be in the ${application.title} application directory.

3. In a terminal, run:
    `<${application.title} installation directory>/bin/start-${application.name}.sh`

4. In your browser go to:
    `http://localhost:${app.http.port}${app.context}`


### Windows

1. Edit `<${application.title} installation directory>\bin\setenv.bat`

2. Set `${app.home.property}` by uncommenting the `${app.home.property}` line and adding the
   absolute path to the directory where you want ${application.title} to store your data.
   This path MUST NOT be in the ${application.title} application directory.

3. In a terminal, run:
    `<${application.title}  installation directory>\bin\start-${application.name}.bat`

4. In your browser go to:
    `http://localhost:${app.http.port}${app.context}`



