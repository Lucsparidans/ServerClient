package Client;

public class ParseActionTest {

    public static void main(String[] args) {
        String a1 = "ADD [FROMACCOUNT] [TOACCOUNT] [AMOUNT]";
        String a2 = "SUB [FROMACCOUNT] [AMOUNT]";
        parseAction(a1);
        parseAction(a2);
    }

    private static void parseAction(String action){
        String[] parts = action.toString().split("\\[");
        System.out.println(parts.length);
        String actionType = parts[0];
        actionType = actionType.replace(" ", "");

        if(parts.length == 3){
            String fromId = parts[1];
            fromId = fromId.replace("] ", "");

            String amount = parts[2];
            amount = amount.replace("]", "");
            System.out.println(actionType+" "+fromId+" "+amount);
        }
        else if(parts.length == 4){
            String fromId = parts[1];
            fromId = fromId.replace("] ", "");

            String toId = parts[2];
            toId = toId.replace("] ", "");

            String amount = parts[3];
            amount = amount.replace("]", "");
            System.out.println(actionType+" "+fromId+" "+toId+" "+amount);
        }
    }
}
