package Client;

public class ParseJSONTest {
    public static void main(String[] args) {
        String[] actions = {
                "SEND [6398C613619E4DCA88220ACA49603D87] [HELLO THOMAS 2]",
                "SEND [EIFERT, THOMAS] [HELLO THOMAS]",
                "SEND [Bank] REGISTER [10000]",
                "SEND [Bank] SUB [6398C613619E4DCA88220ACA49603D87] [1000]",
                "SEND [Bank] ADD [6398C613619E4DCA88220ACA49603D87] [6RHWRU97QJDA4V5GTGNHVSX3CXBN5PDV] [1000]",
                "SEND [Bank] CLOCKS IN",
                "SEND [Bank] SUB [Bank] [10000]",
                "SEND [Bank] SUB [6398C613619E4DCA88220ACA49603D87] [3000]",
                "SEND [Bank] CLOCKS OUT"
        };

        for (String s :
                actions) {
            String[] parts = s.split(" ", 2);
            String actionType = parts[0];
            actionType = actionType.trim();

            String[] p2 = parts[1].split("]",2);

            String toId = p2[0];
            toId = toId.replace("[", "").trim();

            String m = p2[1];

            if(p2[1].charAt(1) == '['){
                m = m.replace("[","");
                m = m.replace("]","");
                System.out.println("NOrg");
            }else{
                System.out.println("Org");
            }
            m = m.trim();
            System.out.printf("ActionType: %s, toID: %s, Message: %s\n",actionType,toId,m);
        }
    }
}
