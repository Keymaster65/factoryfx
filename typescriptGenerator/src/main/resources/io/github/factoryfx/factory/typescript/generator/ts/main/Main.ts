import {RootModel} from "../widget/root/RootModel";
import {WaitAnimationModel} from "../widget/waitannimation/WaitAnimationModel";
import {DynamicDataDictionary} from "../DynamicDataDictionary";
import {NavbarModel} from "../widget/navbar/NavbarModel";
import {DynamicData} from "../DynamicData";
import {NavitemModel} from "../widget/navitem/NavitemModel";
import {FactoryEditorModel} from "../widget/factoryeditor/FactoryEditorModel";
import {HttpClient} from "../HttpClient";
import {ViewModel} from "../widget/view/ViewModel";
import {SaveWidgetModel} from "../widget/save/SaveWidgetModel";
import {FactoryUpdateResultModel} from "../widget/factoryUpdateResult/FactoryUpdateResultModel";
import {DomUtility} from "../DomUtility";
import {ErrorReporterModel} from "../widget/errorreporter/ErrorReporterModel";
import {HttpClientStatusReporter} from "../HttpClientStatusReporter";
import {BootstrapUtility} from "../BootstrapUtility";

export class Main {


    main(target: HTMLElement){
        target.append(BootstrapUtility.createProgressBar())

        let httpClientStatusReporter: HttpClientStatusReporter = new HttpClientStatusReporter();
        let httpClient:  HttpClient= new HttpClient(httpClientStatusReporter);
        httpClient.getMetadata((dynamicDataDictionaryJson: any, guiConfiguration: any) =>{
                let dynamicDataDictionary = new DynamicDataDictionary();
                dynamicDataDictionary.mapFromJson(dynamicDataDictionaryJson);

                httpClient.prepareNewFactory((rootJson: any, baseVersionId: string)=> {
                    let root = new DynamicData();
                    root.mapFromJsonFromRootDynamic(rootJson, dynamicDataDictionary);

                    let factoryEditorModel: FactoryEditorModel = new FactoryEditorModel(httpClient);

                    let navItems: NavitemModel[]=[];
                    for (let navItemsJson of guiConfiguration.navBarItems){
                        let navitemModel: NavitemModel = new NavitemModel(root.getChildFromRoot(navItemsJson.factoryId),factoryEditorModel);
                        navItems.push(navitemModel);

                    }

                    let saveWidgetModel: SaveWidgetModel  = new SaveWidgetModel(baseVersionId,root,httpClient);
                    let navbarModel = new NavbarModel(navItems,factoryEditorModel);
                    let factoryUpdateResultModel: FactoryUpdateResultModel = new FactoryUpdateResultModel();
                    let viewModel: ViewModel = new ViewModel(factoryEditorModel, saveWidgetModel,factoryUpdateResultModel,navbarModel);
                    navbarModel.setViewModel(viewModel);
                    saveWidgetModel.setViewModel(viewModel);
                    let rootModel: RootModel = new RootModel(viewModel,new WaitAnimationModel(), new ErrorReporterModel());


                    viewModel.showFactoryEditor();
                    factoryEditorModel.edit(root);

                    httpClientStatusReporter.setRootModel(rootModel);

                    DomUtility.clear(target);
                    target.append(rootModel.getWidget().render());

                });
            }
        );

    }

    // export class GuiConfiguration {
    // constructor(private guiConfigurationJson: any, private factoryEditor: FactoryEditor, private root: Data, private baseVersionId: string, private view: View, private waitAnimation: WaitAnimation){
    //
    // }
    //
    // public createNavbar(): Navbar{
    //     let navItems: NavItem[]=[];
    //     for (let navItemsJson of this.guiConfigurationJson.navBarItems){
    //         navItems.push(new NavItem(this.root.getChildFromRoot(navItemsJson.factoryId),this.factoryEditor));
    //     }
    //     return new Navbar(this.guiConfigurationJson.projectName,navItems,this.factoryEditor,this.view,new SaveWidget( this.root,this.baseVersionId,this.view,this.waitAnimation));
    // }


}