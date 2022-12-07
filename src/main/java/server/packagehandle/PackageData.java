package server.packagehandle;

import org.json.JSONObject;

public class PackageData {

    public String type;
    public JSONObject data;

    public PackageData(String type, JSONObject data) {
        this.type = type;
        this.data = data;
    }

}
