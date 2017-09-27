/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.oracle.ridc.ucm;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebParam;
import oracle.stellent.ridc.IdcClient;
import oracle.stellent.ridc.IdcClientException;
import oracle.stellent.ridc.IdcClientManager;
import oracle.stellent.ridc.IdcContext;
import oracle.stellent.ridc.model.DataBinder;
import oracle.stellent.ridc.model.DataObject;
import oracle.stellent.ridc.model.DataResultSet;
import oracle.stellent.ridc.model.TransferFile;
import oracle.stellent.ridc.protocol.ServiceResponse;

/**
 *
 * @author zhussipov_34096
 */
public class RidcObj {
    private String IDC_CONNECTION_URL = "idc://blah-blah.com:4445";
    private String UCM_USERNAME = "ucm_username";
    private String UCM_PASSWORD = "ucm_password";
    private IdcClientManager CLIENT_MANAGER;
    private IdcContext USER_CONTEXT;
    private IdcClient CLIENT;

    private final String IDC_SERVICE = "IdcService";
    private String RIDC_DOC_PATH = "/Contribution Folder/";
    private IdcContext userContext;

    public String getIdcConnectionURL() {
        return IDC_CONNECTION_URL;
    }

    public void setIdcConnectionURL(String idcConnectionURL) {
        this.IDC_CONNECTION_URL = idcConnectionURL;
    }

    public String getUcm_username() {
        return UCM_USERNAME;
    }

    public void setUcm_username(String ucm_username) {
        this.UCM_USERNAME = ucm_username;
    }

    public String getUcm_password() {
        return UCM_PASSWORD;
    }

    public void setUcm_password(String ucm_password) {
        this.UCM_PASSWORD = ucm_password;
    }

    public IdcClientManager getClientManager() {
        return CLIENT_MANAGER;
    }

    public void setClientManager(IdcClientManager clientManager) {
        this.CLIENT_MANAGER = clientManager;
    }

    public IdcContext getUserContext() {
        return USER_CONTEXT;
    }

    public void setUserContext(IdcContext userContext) {
        this.USER_CONTEXT = userContext;
    }

    public IdcClient getClient() {
        return CLIENT;
    }

    public void setClient(IdcClient client) {
        this.CLIENT = client;
    }

    public String getIDC_SERVICE() {
        return IDC_SERVICE;
    }

    public String getRIDC_DOC_PATH() {
        return RIDC_DOC_PATH;
    }

    public void setRIDC_DOC_PATH(String RIDC_DOC_PATH) {
        this.RIDC_DOC_PATH = RIDC_DOC_PATH;
    }

    public RidcObj(String idcConnectionURL, String ucm_username, String ucm_password) {
        try {
            this.setIdcConnectionURL(idcConnectionURL);
            this.setUcm_username(ucm_username);
            this.setUcm_password(ucm_password);
            this.setClientManager(new IdcClientManager());
            this.setClient(this.getClientManager().createClient(idcConnectionURL));
            this.setUserContext(new IdcContext(ucm_username, ucm_password));
        } catch (IdcClientException ex) {
            Logger.getLogger(RidcObj.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String pingServer(){

        String result;
        try {
            DataBinder dataBinder = this.getClient().createBinder();
            dataBinder.putLocal(IDC_SERVICE, "PING_SERVER");
            ServiceResponse response = this.getClient().sendRequest(this.getUserContext(), dataBinder);
            DataBinder responseData = response.getResponseAsBinder();
            result = String.format("Ping Server response: %s", responseData.getLocal("StatusMessage"));
            return result;
        } catch (IdcClientException ex) {
            Logger.getLogger(UcmService.class.getName()).log(Level.SEVERE, null, ex);
            return "Unable to initialize connection " + this.getIdcConnectionURL();
        }
    }

    public HashMap<String, String> docInfo(@WebParam(name = "dID") String dID){

        HashMap<String, String> result = new HashMap<String, String>();

        try {
            DataBinder serviceBinder = this.getClient().createBinder();

            serviceBinder.putLocal(IDC_SERVICE, "DOC_INFO");
            serviceBinder.putLocal("dID", dID);
            ServiceResponse serverResponse = this.getClient().sendRequest(this.getUserContext(), serviceBinder);
            DataBinder resBinder = serverResponse.getResponseAsBinder();

            DataResultSet drs = resBinder.getResultSet("DOC_INFO");
            for (DataObject dataObject : drs.getRows()) {
                result.put("dDocTitle", dataObject.get("dDocTitle"));
                result.put("dOriginalName", dataObject.get("dOriginalName"));
                result.put("dFormat", dataObject.get("dFormat"));
                result.put("dExtension", dataObject.get("dExtension"));
            }

        } catch (IdcClientException e) {
            System.out.print(e.getMessage());
        }

        return result;

    }

    public String deleteFile(@WebParam(name = "dID") String dID) {
        ServiceResponse myServiceResponse = null;
        try {
            DataBinder serviceBinder = this.getClient().createBinder();
            serviceBinder.putLocal(IDC_SERVICE, "DELETE_DOC");
            serviceBinder.putLocal("dID", dID);
            myServiceResponse = this.getClient().sendRequest(this.getUserContext(), serviceBinder);
            return "File deleted successfully";
        } catch (IdcClientException idcce) {
            Logger.getLogger(UcmService.class.getName()).log(Level.SEVERE, null, idcce);
            return "Unable to initialize connection " + this.getIdcConnectionURL();
        } catch (Exception e) {
            Logger.getLogger(UcmService.class.getName()).log(Level.SEVERE, null, e);
            return "Exception occurred. Unable to delete file. Message: "+ e.getMessage();
        } finally {
            if (myServiceResponse != null) {
                myServiceResponse.close();
            }
        }
    }

    public String createFolderFld(@WebParam(name = "folderName") String folderName) {
        try {
            DataBinder dataBinder = this.getClient().createBinder();

            dataBinder.putLocal("fParentGUID", this.getFolderIdFromPathFLD(RIDC_DOC_PATH));
            dataBinder.putLocal(IDC_SERVICE, "FLD_CREATE_FOLDER");
            dataBinder.putLocal("fFolderName", folderName);
            dataBinder.putLocal("fSecurityGroup", "Public");
            dataBinder.putLocal("fOwner", this.getUserContext().getUser());

            DataBinder serverBinder2 = this.getClient().sendRequest(this.getUserContext(), dataBinder).getResponseAsBinder();
            return "Success";
        } catch (IdcClientException e) {
            Logger.getLogger(UcmService.class.getName()).log(Level.SEVERE, null, e);
            return "Error";
        }
    }

    public String getFolderIdFromPathFLD(String path) {
        String folderId = new String();
        try {
            DataBinder dataBinder = this.getClient().createBinder();
            dataBinder.putLocal("IdcService", "FLD_INFO");
            dataBinder.putLocal("path", path);
            ServiceResponse response = this.getClient().sendRequest(this.getUserContext(), dataBinder);
            DataBinder responseData = response.getResponseAsBinder();
            DataResultSet resultSet = responseData.getResultSet("FolderInfo");
            for (DataObject dataObject : resultSet.getRows()) {
                folderId = dataObject.get("fFolderGUID");
            }

        } catch (IdcClientException e) {
            Logger.getLogger(UcmService.class.getName()).log(Level.SEVERE, null, e);
        }
        return folderId;
    }

    public String getFolderIdFromPathCollection(String path) throws IOException {
        String folderId = null;
        try {
            DataBinder dataBinder = this.getClient().createBinder();
            dataBinder.putLocal("IdcService", "COLLECTION_INFO");
            dataBinder.putLocal("hasCollectionPath", "true");
            dataBinder.putLocal("dCollectionPath", path);
            ServiceResponse response = this.getClient().sendRequest(this.getUserContext(), dataBinder);
            DataBinder binder = response.getResponseAsBinder();
            DataObject da = binder.getLocalData();
            folderId = da.get("dCollectionID");

        } catch (IdcClientException e) {
            Logger.getLogger(UcmService.class.getName()).log(Level.SEVERE, null, e);
        }
        return folderId;
    }

    public String createFolderCollection(@WebParam(name = "folderName") String folderName){

        try {
            DataBinder dataBinder = this.getClient().createBinder();
            dataBinder.putLocal("IdcService", "COLLECTION_ADD");
            dataBinder.putLocal("hasParentCollectionID", "true");
            dataBinder.putLocal("dParentCollectionID", this.getFolderIdFromPathCollection(RIDC_DOC_PATH));
            dataBinder.putLocal("dCollectionName", folderName);
            dataBinder.putLocal("dSecurityGroup", "Public");
            dataBinder.putLocal("dCollectionOwner", this.getUserContext().getUser());
            DataBinder serverBinder2 = this.getClient().sendRequest(this.getUserContext(), dataBinder).getResponseAsBinder();
            DataObject da = serverBinder2.getLocalData();
            return "Success";
        } catch (IOException | IdcClientException ex) {
            Logger.getLogger(UcmService.class.getName()).log(Level.SEVERE, null, ex);
            return "Error";
        }
        /*
        IdcService=COLLECTION_NEW
        hasParentCollectionID=true
        dParentCollectionID=1
        dCollectionInherit=0
         */
    }

    public String deleteFolderFLD(String FOLDER_NAME) {
        try {
            DataBinder dataBinder = this.getClient().createBinder();
            dataBinder.putLocal("IdcService", "FLD_DELETE");
            dataBinder.putLocal("item1", "fFolderGUID:" + this.getFolderIdFromPathFLD(RIDC_DOC_PATH + FOLDER_NAME));
            DataBinder serverBinder2 = this.getClient().sendRequest(this.getUserContext(), dataBinder).getResponseAsBinder();
            DataObject da = serverBinder2.getLocalData();
            return "Success";
        } catch (IdcClientException ex) {
            Logger.getLogger(UcmService.class.getName()).log(Level.SEVERE, null, ex);
            return "Error";
        }
    }

    public String deleteFolderCollection(String FOLDER_NAME) {
        try {
            DataBinder dataBinder = this.getClient().createBinder();
            dataBinder.putLocal("IdcService", "COLLECTION_DELETE");
            dataBinder.putLocal("hasCollectionPath", "true");
            dataBinder.putLocal("dCollectionPath", RIDC_DOC_PATH + FOLDER_NAME);
            DataBinder serverBinder2;
            serverBinder2 = this.getClient().sendRequest(this.getUserContext(), dataBinder).getResponseAsBinder();
            DataObject da = serverBinder2.getLocalData();
            return "Success";
        } catch (IdcClientException ex) {
            Logger.getLogger(UcmService.class.getName()).log(Level.SEVERE, null, ex);
            return "Error";
        }
    }

    public String renameFolderFLD(String oldName, String newName) {
        String PATH = RIDC_DOC_PATH + oldName;
        try {

            DataBinder dataBinder = this.getClient().createBinder();
            dataBinder.putLocal("IdcService", "FLD_EDIT_FOLDER");   //
            dataBinder.putLocal("fFolderGUID", this.getFolderIdFromPathFLD(PATH));
            dataBinder.putLocal("fFolderName", newName);
            DataBinder serverBinder2 = this.getClient().sendRequest(this.getUserContext(), dataBinder).getResponseAsBinder();
            DataObject da = serverBinder2.getLocalData();
            return "Success";
        } catch (IdcClientException ex) {
            Logger.getLogger(UcmService.class.getName()).log(Level.SEVERE, null, ex);
            return "Error";
        }
    }

    public String renameFolderCollection(String oldName, String newName) {

        String PATH = RIDC_DOC_PATH + oldName;
        System.out.println("PATH is: " + PATH);
        try {

            DataBinder dataBinder = this.getClient().createBinder();
            dataBinder.putLocal("IdcService", "COLLECTION_UPDATE");   //
            dataBinder.putLocal("hasCollectionPath", "true");
            dataBinder.putLocal("dCollectionPath", PATH);
            dataBinder.putLocal("dCollectionName", newName);
            DataBinder serverBinder2 = this.getClient().sendRequest(this.getUserContext(), dataBinder).getResponseAsBinder();
            DataObject da = serverBinder2.getLocalData();
            return "Success";
        } catch (IdcClientException ex) {
            Logger.getLogger(UcmService.class.getName()).log(Level.SEVERE, null, ex);
            return "Error";
        }
    }

    private ServiceResponse downloadFile(String dID) {
        ServiceResponse response = null;
        String idcFile = null;
        try {
            DataBinder serviceBinder = this.getClient().createBinder();
            serviceBinder.putLocal("IdcService", "GET_FILE");
            serviceBinder.putLocal("dID", dID);
            try {
                response = this.getClient().sendRequest(this.getUserContext(), serviceBinder);
                idcFile = response.getHeader("idc-file");
                System.out.println("idcFile = " + idcFile);
            } catch (IdcClientException e) {
                Logger.getLogger(UcmService.class.getName()).log(Level.SEVERE, null, e);
            }

            if (idcFile == null) {
                System.out.println("null");
                return null;
            }

            serviceBinder = this.getClient().createBinder();
            serviceBinder.putLocal("IdcService", "DOC_INFO");
            serviceBinder.putLocal("dID", dID);
            ServiceResponse serverResponse = this.getClient().sendRequest(this.getUserContext(), serviceBinder);
            DataBinder resBinder = serverResponse.getResponseAsBinder();

            DataResultSet drs = resBinder.getResultSet("DOC_INFO");
            for (DataObject dataObject : drs.getRows()) {
                System.out.println(" dDocTitle is: " + dataObject.get("dDocTitle"));          // 1.txt
                System.out.println(" dOriginalName is: " + dataObject.get("dOriginalName"));  // 1.txt
                System.out.println(" dFormat is: " + dataObject.get("dFormat"));              // text/plain
                System.out.println(" dExtension ID is: " + dataObject.get("dExtension"));     // txt
            }

        } catch (IdcClientException e) {
            Logger.getLogger(UcmService.class.getName()).log(Level.SEVERE, null, e);
        }
        return response;
    }

    private String uploadFile(String docTitle, String filePath) {
        ServiceResponse response = null;
        String dDocID = new String();
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return "File " + filePath + " does not exist";
            }

            DataBinder requestData = this.getClient().createBinder();
            requestData.putLocal("IdcService", "CHECKIN_NEW");
            requestData.putLocal("dDocType", "Document");
            requestData.putLocal("dDocTitle", docTitle);
            requestData.putLocal("dDocAuthor",  this.getUserContext().getUser());
            requestData.putLocal("dSecurityGroup", "Public");

            try {
                requestData.addFile("primaryFile", new TransferFile(file));
            } catch (IOException ex) {
                Logger.getLogger(UcmService.class.getName()).log(Level.SEVERE, null, ex);
            }
            response = this.getClient().sendRequest(userContext, requestData);
            DataBinder responseBinder = response.getResponseAsBinder();
            dDocID = responseBinder.getLocal("dID");
            return dDocID;
        } catch (IdcClientException ex) {
            Logger.getLogger(UcmService.class.getName()).log(Level.SEVERE, null, ex);
            return "Error-0000";
        }
    }

}
