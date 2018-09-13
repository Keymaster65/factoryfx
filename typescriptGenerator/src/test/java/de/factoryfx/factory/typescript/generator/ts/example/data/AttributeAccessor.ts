//generated code don't edit manually
import AttributeMetadata from "./AttributeMetadata";
import Data from "./Data";

export default class AttributeAccessor<T, P extends Data> {
    attributeMetadata: AttributeMetadata<T>;
    attributeName: string;
    attributeParent: P;

    constructor(attributeMetadata: AttributeMetadata<T>, attributeParent: P, attributeName: string) {
        this.attributeMetadata = attributeMetadata;
        this.attributeParent = attributeParent;
        this.attributeName = attributeName;
    }
}