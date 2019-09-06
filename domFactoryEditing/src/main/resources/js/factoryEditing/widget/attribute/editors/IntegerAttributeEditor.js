import { AttributeEditorWidget } from "../AttributeEditorWidget";
export class IntegerAttributeEditor extends AttributeEditorWidget {
    constructor(attributeAccessor, inputId) {
        super(attributeAccessor, inputId);
        this.attributeAccessor = attributeAccessor;
        this.inputId = inputId;
    }
    renderAttribute() {
        let input = document.createElement("input");
        input.id = this.inputId.toString();
        input.className = "form-control";
        input.type = "number";
        input.max = "2147483647";
        input.min = "-2147483648";
        let value = this.attributeAccessor.getValue();
        if (value !== null && value !== undefined) {
            input.valueAsNumber = value;
        }
        input.oninput = (e) => {
            this.attributeAccessor.setValue(input.valueAsNumber);
        };
        input.required = !this.attributeAccessor.getAttributeMetadata().nullable();
        return input;
    }
}