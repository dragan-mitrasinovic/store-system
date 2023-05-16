package rs.etf.sab;

import rs.etf.sab.operations.*;
import rs.etf.sab.student.*;
import rs.etf.sab.tests.*;

public class StudentMain {

    public static void main(String[] args) {

        ArticleOperations articleOperations = new md190062_ArticleOperations();
        BuyerOperations buyerOperations = new md190062_BuyerOperations();
        CityOperations cityOperations = new md190062_CityOperations();
        GeneralOperations generalOperations = new md190062_GeneralOperations();
        OrderOperations orderOperations =
                new md190062_OrderOperations(generalOperations, buyerOperations);
        ShopOperations shopOperations = new md190062_ShopOperations();
        TransactionOperations transactionOperations = new md190062_TransactionOperations();

        TestHandler.createInstance(
                articleOperations,
                buyerOperations,
                cityOperations,
                generalOperations,
                orderOperations,
                shopOperations,
                transactionOperations);

        TestRunner.runTests();
    }
}
