echo "Creating CnCNonPayloadMonitoringProfile monitoring profile"
mqsicreateconfigurableservice ${MQSI_BROKER_NAME} -c MonitoringProfiles -o CnCNonPayloadMonitoringProfile
echo "CnCNonPayloadMonitoringProfile monitoring profile created successfully"
echo "Creating CnCPayloadMonitoringProfile monitoring profile"
mqsicreateconfigurableservice ${MQSI_BROKER_NAME} -c MonitoringProfiles -o CnCPayloadMonitoringProfile
echo "CnCPayloadMonitoringProfile monitoring profile created successfully"
echo "Importing CnCNonPayloadMonitoringProfile profile"
mqsichangeproperties ${MQSI_BROKER_NAME} -c MonitoringProfiles -o CnCNonPayloadMonitoringProfile -n profileProperties -p /var/mqm/scripts/${MQSI_BROKER_NAME}/CnCNonPayloadMonitoringProfile.xml
echo "Imported CnCNonPayloadMonitoringProfile successfully"
echo "Importing CnCPayloadMonitoringProfile profile"
mqsichangeproperties ${MQSI_BROKER_NAME} -c MonitoringProfiles -o CnCPayloadMonitoringProfile -n profileProperties -p /var/mqm/scripts/${MQSI_BROKER_NAME}/CnCPayloadMonitoringProfile.xml
echo "Imported CnCPayloadMonitoringProfile profile successfully"