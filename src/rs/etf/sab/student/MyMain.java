package rs.etf.sab.student;

public class MyMain {

    public static void main(String[] args) {
        md190062_ArticleOperations articleOperations = new md190062_ArticleOperations();
        md190062_BuyerOperations buyerOperations = new md190062_BuyerOperations();
        md190062_CityOperations cityOperations = new md190062_CityOperations();
        md190062_GeneralOperations generalOperations = new md190062_GeneralOperations();
        md190062_OrderOperations orderOperations =
                new md190062_OrderOperations(generalOperations, buyerOperations);
        md190062_ShopOperations shopOperations = new md190062_ShopOperations();
        md190062_TransactionOperations transactionOperations = new md190062_TransactionOperations();

        generalOperations.eraseAll();
    }
}
