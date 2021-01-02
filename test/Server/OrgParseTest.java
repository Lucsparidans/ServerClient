package Server;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public class OrgParseTest {
    private static final String ORG_PATH = "src/JSON_files/organizations.json";
    public static void main(String[] args) {
        JSONParser jsonParser = new JSONParser();
        try{
            JSONObject data = (JSONObject) jsonParser.parse(new FileReader(ORG_PATH));
            JSONArray Organisations = (JSONArray) data.get("Organizations");
            for (Object object :
                    Organisations) {
                JSONObject org = (JSONObject) object;
                parseOrg(org);
            }
            System.out.println();
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void parseOrg(JSONObject organisation){
        Double balance = Double.parseDouble((String) organisation.get("balance"));
        JSONArray roles = (JSONArray) organisation.get("roles");
        String name = (String) organisation.get("name");
        JSONArray employees = (JSONArray) organisation.get("employees");
    }
}
