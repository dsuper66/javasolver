import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReadCaseFile {
    static final InputFieldMapping inputFieldMapping = new InputFieldMapping();
    static final ModelDefService modelDefService = new ModelDefService();
    //static ModelElementDataService modelElementDataService = new ModelElementDataService();

    public static void readCase(ModelDataService modelDataService) throws IOException {

        //DAILY
        //here enode is mapped to pnode
        //Map a field name to an element type
        String sectionName = "PNODE";
        inputFieldMapping.addFieldElementMap(sectionName,"PNODENAME","pnode",1);
        inputFieldMapping.addFieldElementMap(sectionName,"KEY1","mktEnode",1);
        inputFieldMapping.addFieldElementMap(sectionName,"KEY2","mktEnode",2);
        inputFieldMapping.addFieldElementMap(sectionName,"KEY3","mktEnode",3);
        //PROPERTY Map
        inputFieldMapping.addFieldPropertyMap(
                sectionName,"FACTOR","factorPnodeMktEnode");

        //---STATIC MSSNET---
        //here the network enode is mapped to enode
        sectionName = "NODE";
        inputFieldMapping.addFieldElementMap(sectionName,"ID_ENODE","nwEnode",1);
        inputFieldMapping.addFieldElementMap(sectionName,"ID_ST","mktEnode",1);
        inputFieldMapping.addFieldElementMap(sectionName,"ID_KV","mktEnode",2);
        inputFieldMapping.addFieldElementMap(sectionName,"ID_EQUIPMENT","mktEnode",3);
        inputFieldMapping.addFieldPropertyMap(
                sectionName,"ID_ENODE","nwEnodeForMktEnode");

        //----TIME-BASED MSSNET----
        //network enode to bus
        //I,NETDATA,ENODEBUS,1.0,ID_ENODE,ID_BUS,ID_KV,ID_ST,ELECTRICAL_ISLAND,REFERENCE
        sectionName = "ENODEBUS";
        inputFieldMapping.addFieldElementMap(sectionName,"ID_ENODE","nwEnode",1);
        inputFieldMapping.addFieldElementMap(sectionName,"ID_BUS","bus",1);
        //properties
        inputFieldMapping.addFieldPropertyMap(sectionName,"ID_BUS","busForNwEnode");
        inputFieldMapping.addFieldPropertyMap(
                sectionName,"ELECTRICAL_ISLAND","electricalIsland");
        //branch to bus
        //I,NETDATA,BRANCHBUS,1.0,ID_BRANCH,ID_FROMBUS,ID_TOBUS,SUSCEPTANCE,RESISTANCE,REMOVE
        sectionName = "BRANCHBUS";
        inputFieldMapping.addFieldElementMap(sectionName,"ID_BRANCH","branch",1);
        //properties
        inputFieldMapping.addFieldPropertyMap(
                sectionName,"ID_FROMBUS","fromBus");
        inputFieldMapping.addFieldPropertyMap(
                sectionName,"ID_TOBUS","toBus");
        inputFieldMapping.addFieldPropertyMap(
                sectionName,"SUSCEPTANCE","susceptance");
        inputFieldMapping.addFieldPropertyMap(
                sectionName,"RESISTANCE","resistance");

        //----PERIOD----
        //BIDSANDOFFERS,1.0,PNODENAME,TRADERID,INTERVAL,TRADETYPE,TRADERBLOCKALTKEY,TRADERBLOCKTRANCHE,
        // TRADERBLOCKLIMIT,TRADERBLOCKPRICE,SIXSEC,RESERVEPERCENT,DISPATCHABLE
        sectionName = "BIDSANDOFFERS";
        inputFieldMapping.addFieldElementMap(sectionName,"PNODENAME","pnode",1);
        inputFieldMapping.addFieldElementMap(sectionName,"TRADERBLOCKALTKEY","tranche",1);
        inputFieldMapping.addFieldElementMap(sectionName,"TRADERBLOCKTRANCHE","tranche",2);
        //Map a field name to a property type
        inputFieldMapping.addFieldPropertyMap(sectionName,"TRADETYPE", "trancheType");
        inputFieldMapping.addFieldPropertyMap(sectionName, "TRADERBLOCKLIMIT", "trancheLimit");
        inputFieldMapping.addFieldPropertyMap(sectionName, "TRADERBLOCKPRICE", "tranchePrice");
        inputFieldMapping.addFieldPropertyMap(sectionName, "PNODENAME", "tranchePnode");
        //pnode load
        //I,MSSDATA,PNODELOAD,1.0,PNODENAME,INTERVAL,LOADAREAID,ACTUALLOAD,SOURCEOFACTUAL,INSTRUCTEDSHED,
        // CONFORMINGFACTOR,NONCONFORMINGLOAD,CONFORMINGFORECAST,ISNCL,ISBAD,ISOVERRIDE,INSTRUCTEDSHEDACTIVE,DISPATCHEDLOAD,DISPATCHEDGEN
        sectionName = "PNODELOAD";
        inputFieldMapping.addFieldElementMap(sectionName,"PNODENAME","pnode",1);
        inputFieldMapping.addFieldPropertyMap(sectionName,"ACTUALLOAD", "pnodeLoad");

        //----MSSMOD----
        //I,MSSDATA,BRANCHLIMIT,1.0,INTERVAL,KEY1,KEY2,KEY3,KEY4,
        // BASECASEMWLIMITFOR,BASECASEMWLIMITFOROVRDN,BASECASEMWLIMITREV,BASECASEMWLIMITREVOVRDN,FIXEDLOSS
        sectionName = "BRANCHLIMIT";
        inputFieldMapping.addFieldElementMap(sectionName,"KEY1","mktBranch",1);
        inputFieldMapping.addFieldElementMap(sectionName,"KEY2","mktBranch",2);
        inputFieldMapping.addFieldElementMap(sectionName,"KEY3","mktBranch",3);
        inputFieldMapping.addFieldPropertyMap(sectionName,"BASECASEMWLIMITFOR", "mktBrLimit");

        //Interval 10:00
        LocalDateTime caseInterval =
                LocalDateTime.of(2021, 7, 6,10,0);

        //Case file names
        //"/Users/davidbullen/java/MSS_51112021071200687_0X/MSS_51112021071200687_0X.DAILY";
        ///Users/davidbullen/java/MSS_51112021071200687_0X/MSS_51112021071200687_0X.MSSNET
        //String caseFileDir = "/Users/davidbullen/java/MSS_51112021071200687_0X/";
        String caseFileDir = "/Users/davidbullen/java/small-model/";
        String caseId = "MSS_51112021071200687_0X";

        //Make the file name for time based MSSNET
        //MSS_51112021071200687_0X_06-JUL-2021_10_00_0.MSSNET
        DateTimeFormatter dateFormatter
                = DateTimeFormatter.ofPattern("dd-MMM-yyyy_hh_mm", Locale.ENGLISH);
        String timeBasedMSSNET = "_" + caseInterval.format(dateFormatter).toUpperCase() + "_0.MSSNET";

        //File types to read
        List<String> casefileTypes = List.of(".DAILY", ".PERIOD",".MSSNET",timeBasedMSSNET,".MSSMOD");

        //Set the interval for filtering in the file if it has an interval field
        //06-JUL-2021 00:00
        dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm", Locale.ENGLISH);
        String caseIntervalInFile = caseInterval.format(dateFormatter).toUpperCase();
        System.out.println(">>>>"+ caseIntervalInFile);

        //Read the different case types
        for (String caseType : casefileTypes){
            String fileName = caseFileDir + caseId + caseType;
            BufferedReader bufferedReader =
                    new BufferedReader(new FileReader(fileName));
            System.out.println("------ file:" + fileName + " -------");

            String curLine;
            HashMap<String, Map<Integer, Integer>> elementTypeFieldMaps = new HashMap<>();
            HashMap<String, Integer> propertyTypeFieldMaps = new HashMap<>();

            boolean dataIsIntervalBased = false; //If interval based then filter data on interval
            while ((curLine = bufferedReader.readLine()) != null) {

                //HEADER: Get the headers and see which match elements and properties
                //String thisSectionName = "";
                if (curLine.startsWith("I")) {

                    //System.out.println(curLine);
                    List<String> thisRowfieldNames = Arrays.asList(curLine.split(","));

                    String thisSectionName = thisRowfieldNames.get(2);
                    //ELEMENT mapping
                    elementTypeFieldMaps = inputFieldMapping.getElementFieldMapForSectionFieldNames(
                            thisSectionName, thisRowfieldNames);
                    if (elementTypeFieldMaps.size() > 0) {
                        System.out.println("elementTypeFieldMaps:" + elementTypeFieldMaps + " sectionName:" + thisSectionName);
                    }

                    //PROPERTY mapping
                    propertyTypeFieldMaps = inputFieldMapping.getPropertyFieldMapForSectionFieldNames(
                            thisSectionName,thisRowfieldNames);
                    if (elementTypeFieldMaps.size() > 0) {
                        System.out.println("propertyTypeFieldMaps:" + propertyTypeFieldMaps);
                    }

                    //Interval based data may need to be filtered by interval
                    dataIsIntervalBased = curLine.contains("INTERVAL");
                    //System.out.println("dataIsIntervalBased:" + dataIsIntervalBased);
                }

                //DATA: Create the elements and properties
                else if (curLine.startsWith("D") && (!dataIsIntervalBased || curLine.contains(caseIntervalInFile))) {
                    List<String> thisRowData = Arrays.asList(curLine.split(","));

                    //ELEMENT
                    //The elementId is a concatenation of the fields for the elementType
                    //For each element type that has a mapping from the header processing
                    //create the elementId
                    HashMap<String, String> elementTypeAndIdFromThisRow = new HashMap<>();

                    for (String elementType : elementTypeFieldMaps.keySet()) {
                        Map<Integer, Integer> orderNumFieldNum = elementTypeFieldMaps.get(elementType);
                        String elementId = "";
                        //Create the elementId from one or more fields
                        for (Integer orderNum : orderNumFieldNum.keySet()) {
                            Integer fieldNum = orderNumFieldNum.get(orderNum);
                            String trimmedData = thisRowData.get(fieldNum - 1).stripLeading().stripTrailing();
                            //If multiple components then "x" between
                            elementId += ((elementId.length() > 0) ? "x" : "") + trimmedData; //thisRowData.get(fieldNum - 1);
                        }
                        //replace spaces within
                        elementId = elementId.replaceAll("\\s{1,}", "v").trim();
                        //Note that these means only one type of each element per row
                        //elementTypeAndIdFromThisRow.put(elementType, elementId.stripTrailing());
                        elementTypeAndIdFromThisRow.put(elementType, elementId);
                    }
                    //System.out.println("elementTypes:" + elementIdAndTypeToAdd);

                    //Add the elements
                    for (var elementTypeAndId : elementTypeAndIdFromThisRow.entrySet()) {
                        modelDataService.addElement(
                                elementTypeAndId.getKey(),elementTypeAndId.getValue());
                    }

                    //Get and assign the Properties

                    for (String propertyTypeId : propertyTypeFieldMaps.keySet()) {
                        Integer fieldNum = propertyTypeFieldMaps.get(propertyTypeId);
                        //replace spaces
                        String trimmedData =
                              thisRowData.get(fieldNum - 1).stripLeading().stripTrailing()
                                    .replaceAll("\\s{1,}", "v").trim();
                        //System.out.println("propertyType:" + propertyType + " propertyValue:" + propertyValue);

                        //If any of the element types have this property then assign the value
                        /*
                        for (var elementIdTypeMap : elementIdTypeMapFromThisRow.entrySet()) {
                            Boolean elementHasThisProperty = modelDefService.elementTypeHasProperty(
                                            elementIdTypeMap.getValue(), propertyTypeId);

                            if (elementHasThisProperty) {

                                modelDataService.assignElementProperty(
                                        elementIdTypeMap.getKey(), propertyTypeId, fieldValue);
                            }
                        }*/

                        //If we have all elements that match the property type then assign the value
                        //Get the property def for the property type that was found

                        modelDefService.propertyTypeDef(propertyTypeId).ifPresent(propertyTypeDef -> {
                            ArrayList<String> elementIds = new ArrayList<>();
                            boolean foundAllElementTypes = true;
                            for (String elementType : propertyTypeDef.elementTypes) {
                                //System.out.println("propertyType:" + propertyTypeId + " elementType:" + elementType);
                                String elementId = elementTypeAndIdFromThisRow.get(elementType);
                                if (elementId != null) {
                                    elementIds.add(elementId);
                                } else {
                                    foundAllElementTypes = false;
                                    break;
                                }
                            }
                            if (foundAllElementTypes) {
                                String thisSectionName = thisRowData.get(2);
                                /*if (thisSectionName.equals("NODE")) {
                                    System.out.println(thisSectionName + " read:"
                                            + propertyTypeId + "(" + elementIds + ") = " + fieldValue);
                                }*/
                                //Add the property
                                modelDataService.addProperty(propertyTypeId, elementIds, trimmedData);
                            }
                        });
                    }
                }
            }
            bufferedReader.close();
        }
    }
}
