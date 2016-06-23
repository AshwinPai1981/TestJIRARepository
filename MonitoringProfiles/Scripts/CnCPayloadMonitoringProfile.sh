echo "Applying monitoring profiles"
mqsichangeflowmonitoring ${MQSI_BROKER_NAME} -e SWEB_CNC1 -k Aisle_Gateway -j -m CnCPayloadMonitoringProfile -c active
mqsichangeflowmonitoring ${MQSI_BROKER_NAME} -e SWEB_CNC1 -k ConfigDetails_Gateway -j -m CnCPayloadMonitoringProfile -c active
mqsichangeflowmonitoring ${MQSI_BROKER_NAME} -e SWEB_CNC1 -k FulfilmentSite_Gateway -j -m CnCPayloadMonitoringProfile -c active
mqsichangeflowmonitoring ${MQSI_BROKER_NAME} -e SWEB_CNC1 -k GoodsMovement_Gateway -j -m CnCPayloadMonitoringProfile -c active
mqsichangeflowmonitoring ${MQSI_BROKER_NAME} -e SWEB_CNC1 -k LayoutModule_Gateway_V2 -j -m CnCPayloadMonitoringProfile -c active
mqsichangeflowmonitoring ${MQSI_BROKER_NAME} -e SWEB_CNC1 -k OutboundDelivery_Gateway -j -m CnCPayloadMonitoringProfile -c active
mqsichangeflowmonitoring ${MQSI_BROKER_NAME} -e SWEB_CNC1 -k Personnel_Gateway -j -m CnCPayloadMonitoringProfile -c active
mqsichangeflowmonitoring ${MQSI_BROKER_NAME} -e SWEB_CNC1 -k PhysicalInventory_Gateway -j -m CnCPayloadMonitoringProfile -c active
mqsichangeflowmonitoring ${MQSI_BROKER_NAME} -e SWEB_CNC1 -k Printer_Gateway -j -m CnCPayloadMonitoringProfile -c active
mqsichangeflowmonitoring ${MQSI_BROKER_NAME} -e SWEB_CNC1 -k Product_Gateway_V2 -j -m CnCPayloadMonitoringProfile -c active
mqsichangeflowmonitoring ${MQSI_BROKER_NAME} -e SWEB_CNC1 -k PurchaseOrder_Gateway -j -m CnCPayloadMonitoringProfile -c active
mqsichangeflowmonitoring ${MQSI_BROKER_NAME} -e SWEB_CNC1 -k ShelfLabelProducts_Gateway -j -m CnCPayloadMonitoringProfile -c active
mqsichangeflowmonitoring ${MQSI_BROKER_NAME} -e SWEB_CNC1 -k Supplier_Gateway -j -m CnCPayloadMonitoringProfile -c active
mqsichangeflowmonitoring ${MQSI_BROKER_NAME} -e SWEB_CNC1 -k Task_Gateway -j -m CnCPayloadMonitoringProfile -c active
mqsichangeflowmonitoring ${MQSI_BROKER_NAME} -e SWEB_CNC1 -k TransferOrder_Gateway -j -m CnCPayloadMonitoringProfile -c active
mqsichangeflowmonitoring ${MQSI_BROKER_NAME} -e SWEB_CNC1 -k Bin_Gateway_V2 -j -m CnCPayloadMonitoringProfile -c active
mqsichangeflowmonitoring ${MQSI_BROKER_NAME} -e SWEB_CNC1 -k Bay_Gateway -j -m CnCPayloadMonitoringProfile -c active
mqsichangeflowmonitoring ${MQSI_BROKER_NAME} -e SWEB_CNC2 -k InboundDelivery_Gateway -j -m CnCPayloadMonitoringProfile -c active
mqsichangeflowmonitoring ${MQSI_BROKER_NAME} -e SWEB_CNC2 -k GoodsReceipt_Gateway -j -m CnCPayloadMonitoringProfile -c active
echo "Monitoring profiles applied and activated"