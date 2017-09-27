/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.oracle.ridc.ucm;

import java.util.HashMap;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.ejb.Stateless;

/**
 *
 * @author dasm
 */
@WebService(serviceName = "UCM")
@Stateless()
public class UcmService {

    private final String idcConnectionURL = "idc://blah-blah.com:4445";
    private final String ucm_username = "ucm_username";
    private final String ucm_password = "ucm_password";
    private RidcObj ridc;

    public String getIdcConnectionURL() {
        return idcConnectionURL;
    }

    public String getUcm_username() {
        return ucm_username;
    }

    public String getUcm_password() {
        return ucm_password;
    }

    public UcmService() {
        ridc = new RidcObj(this.getIdcConnectionURL(), this.getUcm_username(), this.getUcm_password());
    }

    @WebMethod(operationName = "pingServer")
    public String pingServer(){
        return this.ridc.pingServer();
    }

    @WebMethod(operationName = "docInfo")
    public HashMap<String, String> docInfo(@WebParam(name = "dID") String dID){
        return ridc.docInfo(dID);
    }

    @WebMethod(operationName = "deleteFile")
    public String deleteFile(@WebParam(name = "dID") String dID) {
        return ridc.deleteFile(dID);
    }

    @WebMethod(operationName = "createFolderFld")
    public String createFolderFld(@WebParam(name = "folderName") String folderName) {
        return ridc.createFolderFld(folderName);
    }

    @WebMethod(operationName = "createFolderCollection")
    public String createFolderCollection(@WebParam(name = "folderName") String folderName){
        return ridc.createFolderCollection(folderName);
    }

    @WebMethod(operationName = "deleteFolderFLD")
    public String deleteFolderFLD(@WebParam(name = "folderName") String folderName){
        return ridc.deleteFolderFLD(folderName);
    }

    @WebMethod(operationName = "deleteFolderCollection")
    public String deleteFolderCollection(@WebParam(name = "folderName") String folderName){
        return ridc.deleteFolderCollection(folderName);
    }

    @WebMethod(operationName = "renameFolderFLD")
    public String renameFolderFLD(@WebParam(name = "oldName") String oldName, @WebParam(name = "newName") String newName){
        return ridc.renameFolderFLD(oldName, newName);
    }

    @WebMethod(operationName = "renameFolderCollection")
    public String renameFolderCollection(@WebParam(name = "oldName") String oldName, @WebParam(name = "newName") String newName){
        return ridc.renameFolderFLD(oldName, newName);
    }

}
