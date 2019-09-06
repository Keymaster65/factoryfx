import { Widget } from "../../base/Widget";
export class AttributeEditorWidget extends Widget {
    constructor(attributeAccessor, inputId) {
        super();
        this.attributeAccessor = attributeAccessor;
        this.inputId = inputId;
    }
    createLabel() {
        let label = document.createElement("label");
        label.htmlFor = this.inputId;
        label.textContent = this.attributeAccessor.getAttributeMetadata().getLabelText("en");
        label.className = "col-xl-2 col-form-label";
        label.style.textOverflow = "clip";
        label.style.overflow = "hidden";
        return label;
    }
    render() {
        let result = this.renderAttribute();
        this.bindAttribute();
        return result;
    }
    bindModel() {
        this.bindAttribute();
    }
    bindAttribute() {
    }
}
