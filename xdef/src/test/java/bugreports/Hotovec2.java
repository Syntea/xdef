package bugreports;

/**
 *
 * @author Vaclav Trojan
 */
public interface Hotovec2 {
    //---------------------------------------------------------------------------
    OperationForm getOperationForm();
    void setOperationForm(OperationForm operationForm);
    //---------------------------------------------------------------------------
    //   enum OperationForm
    //---------------------------------------------------------------------------
    enum OperationForm {
        DIR,
        TRY,
        STD;
    }
	
}
