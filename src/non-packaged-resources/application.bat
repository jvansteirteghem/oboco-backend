java -XX:+UseG1GC -XX:+UseStringDeduplication -Xms256m -Xmx1024m -cp lib/*;oboco-${project.version}-runner.jar com.gitlab.jeeto.oboco.Main
pause
