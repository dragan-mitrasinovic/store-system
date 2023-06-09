package rs.etf.sab;

import rs.etf.sab.operations.*;
import rs.etf.sab.student.*;
import rs.etf.sab.tests.*;

public class StudentMain {

    public static void main(String[] args) {

        ArticleOperations articleOperations = new ArticleOperationsImpl();
        BuyerOperations buyerOperations = new BuyerOperationsImpl();
        CityOperations cityOperations = new CityOperationsImpl();
        GeneralOperations generalOperations = new GeneralOperationsImpl();
        OrderOperations orderOperations =
                new OrderOperationsImpl(generalOperations, buyerOperations);
        ShopOperations shopOperations = new ShopOperationsImpl();
        TransactionOperations transactionOperations = new TransactionOperationsImpl();

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
