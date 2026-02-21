import java.io.FileReader;
import java.math.BigInteger;
import java.util.*;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Main {

    static class Share {
        BigInteger x;
        BigInteger y;

        Share(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }

    public static void main(String[] args) {
        try {
            FileReader reader = new FileReader("input.json");
            JSONObject json = new JSONObject(new JSONTokener(reader));

            JSONObject keys = json.getJSONObject("keys");
            int n = keys.getInt("n");
            int k = keys.getInt("k");

            // Read all shares
            List<Share> shares = new ArrayList<>();
            for (String key : json.keySet()) {
                if (key.equals("keys")) continue;

                JSONObject shareObj = json.getJSONObject(key);
                int base = Integer.parseInt(shareObj.getString("base"));
                String value = shareObj.getString("value");

                BigInteger x = new BigInteger(key);
                BigInteger y = new BigInteger(value, base);

                shares.add(new Share(x, y));
            }

            // Sort by x
            shares.sort(Comparator.comparing(s -> s.x));

            // Use first k shares
            List<Share> selectedShares = shares.subList(0, k);

            BigInteger secret = lagrangeInterpolationAtZero(selectedShares);

            System.out.println("Reconstructed secret: " + secret);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Lagrange interpolation at x=0
    private static BigInteger lagrangeInterpolationAtZero(List<Share> shares) {
        BigInteger secret = BigInteger.ZERO;

        for (int i = 0; i < shares.size(); i++) {
            BigInteger xi = shares.get(i).x;
            BigInteger yi = shares.get(i).y;

            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < shares.size(); j++) {
                if (i == j) continue;

                BigInteger xj = shares.get(j).x;

                numerator = numerator.multiply(xj.negate());
                denominator = denominator.multiply(xi.subtract(xj));
            }

            BigInteger term = yi.multiply(numerator).divide(denominator);
            secret = secret.add(term);
        }

        return secret;
    }
}