call rd /s /q release
call mvn -Dmaven.test.skip=true clean install


pause