package bugreports;

/**
 *
 * @author Vaclav Trojan
 */
public abstract class Hotovec1 {

    Hotovec2.OperationForm operationForm = null;

    public Hotovec2.OperationForm getOperationForm() {
        return operationForm;
    }

    public void setOperationForm(Hotovec2.OperationForm operationFormEnum) {
        this.operationForm = operationFormEnum;
    }

    protected String getOperationFormString() {
        return operationForm == null ? null : operationForm.toString();
    }

    protected void setOperationFormString(String operationForm) {
        this.operationForm = Hotovec2.OperationForm.valueOf(operationForm);
    }

}