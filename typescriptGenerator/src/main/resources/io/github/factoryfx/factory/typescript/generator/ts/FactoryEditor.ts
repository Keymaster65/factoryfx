import {Data} from "./Data";
import {AttributeEditorCreator} from "./AttributeEditorCreator";
import {DomUtility} from "./DomUtility";
import {Widget} from "./Widget";
import {WaitAnimation} from "./WaitAnimation";


export class FactoryEditor implements Widget {
    form: HTMLFormElement;
    container: HTMLDivElement;

    constructor(private attributeEditorCreator: AttributeEditorCreator, private waitAnimation: WaitAnimation) {
        this.container= document.createElement("div");
    }

    createBreadCrumb(data: Data): HTMLElement {
        let nav: HTMLElement = document.createElement("nav");
        nav.setAttribute("aria-label", "breadcrumb");

        let ol: HTMLOListElement = document.createElement("ol");
        ol.className = "breadcrumb";
        ol.style.borderRadius = "0";

        let counter: number = 0;
        let path = data.getPath();
        for (let pathElement of path) {
            let li: HTMLLIElement = document.createElement("li");
            if (counter == path.length - 1) {
                li.className = "breadcrumb-item active";
                li.textContent = pathElement.getDisplayText();
            } else {
                let a: HTMLAnchorElement = document.createElement("a");
                a.href = "#";
                a.textContent = pathElement.getDisplayText();
                a.onclick = (e) => {
                    e.preventDefault();
                    this.edit(pathElement);
                    return false;
                };


                li.className = "breadcrumb-item";
                li.appendChild(a)
            }
            ol.appendChild(li);
            counter++;
        }

        nav.appendChild(ol);
        return nav;
    }

    factoryChangeEvent: (newData: Data)=>void;

    public setOnFactoryChange(event: (newData: Data)=>void) {
        this.factoryChangeEvent = event;
    }

    private currentData: Data;

    edit(data: Data) {
        this.currentData = data;
        DomUtility.clear(this.container);

        let editDiv: HTMLDivElement = document.createElement("div");



        //You can't construct DOM elements using normal constructors because you're supposed to go through document.createElement

        this.form = document.createElement("form");
        let counter: number = 0;
        for (let listAttributeAccessorElement of data.listAttributeAccessor()) {

            let formGroup: HTMLElement = document.createElement("div");
            formGroup.className = "form-group row";
            formGroup.style.padding = "0rem 1rem";

            let label: HTMLLabelElement = document.createElement("label");
            label.htmlFor = counter.toString();
            label.className = "col-xl-2 col-form-label";
            label.style.textOverflow="clip";
            label.style.overflow="hidden";
            label.textContent = listAttributeAccessorElement.getLabelText("en");

            let div: HTMLDivElement = document.createElement("div");
            div.className = "col-xl-10";
            div.appendChild(this.attributeEditorCreator.create(listAttributeAccessorElement, counter.toString(), this, this.waitAnimation).create());

            formGroup.appendChild(label);
            formGroup.appendChild(div);
            this.form.appendChild(formGroup);

            this.form.appendChild(document.createElement("hr"));
            counter++;
        }
        editDiv.appendChild(this.form);

        if (this.factoryChangeEvent) {
            this.factoryChangeEvent(data);
        }

        this.form = document.createElement("form");


        this.container.className = "container-fluid";
        this.container.style.padding="0px";
        this.container.appendChild(this.createBreadCrumb(data));
        let row: HTMLDivElement = document.createElement("div");
        row.className = "row";

        let col4: HTMLDivElement = document.createElement("div");
        col4.className = "col-4";
        let col8: HTMLDivElement = document.createElement("div");
        col8.className = "col-8";

        this.container.appendChild(row);
        row.appendChild(col4);
        row.appendChild(col8);

        col8.appendChild(editDiv);
        let treeRoot: HTMLElement = this.createTree(data.getRoot());
        treeRoot.style.overflowX="scroll";
        col4.appendChild(treeRoot);


    }

    back() {
        let path: Array<Data> = this.currentData.getPath();
        if (path.length >= 2) {
            this.edit(path[path.length - 2]);
        }
    }

    validate(): boolean {
        return this.form.reportValidity();
    }

    getCurrentData(): Data {
        return this.currentData;
    }

    create(): HTMLElement {
        return this.container;
    }

    private createTree(root: Data): HTMLElement {
        let card: HTMLDivElement = document.createElement("div");
        card.className="card";
        let cardBody: HTMLDivElement = document.createElement("div");
        cardBody.className="card-body";

        cardBody.appendChild(this.createTreeItem(root));
        card.appendChild(cardBody);
        card.style.marginLeft="15px";
        return card;
    }

    private createTreeItem(data: Data): HTMLElement {
        let ul: HTMLUListElement = document.createElement("ul");
        for (let child of data.getChildrenFlat()) {
            let li: HTMLLIElement  = document.createElement("li");

            let a: HTMLAnchorElement = document.createElement("a");
            a.href="#";
            a.textContent=child.getDisplayText();
            a.style.whiteSpace="nowrap";
            a.onclick=(e)=>{
                this.edit(child);
                e.preventDefault();
            };
            li.appendChild(a);

            li.appendChild(this.createTreeItem(child));
            ul.appendChild(li);
        }
        return ul;
    }
}