##### view help: #########################
java -cp [/path/to/h2.jar] org.h2.tools.Server -?

##### start db: #########################
java -cp [/path/to/h2.jar] org.h2.tools.Server -baseDir [/path/to/dir/with/databases] -tcp
#-ifExists

##### backup db: #########################
java -cp $PATH_TO_H2_JAR org.h2.tools.Script -url $DB_URL -user $DB_USER -password $DB_PASSWORD -script $CURR_BACKUP_DIR/$DB_USER-db.zip -options compression zip

##### restore db: #########################
java -cp $H2_JAR org.h2.tools.RunScript -url "jdbc:h2:tcp://localhost:$DB_PORT/$DB_NAME" -user "$USER" -password "$PASSWORD" -script $PATH_TO_ZIPPED_SCRIPT -options compression zip

