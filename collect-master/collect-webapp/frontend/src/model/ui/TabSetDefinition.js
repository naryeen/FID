import { TabContainers } from './TabContainers'
import { UIModelObjectDefinition } from './UIModelObjectDefinition'

export class TabSetDefinition extends UIModelObjectDefinition {
  _uiConfiguration
  rootEntityDefinitionId
  items = []
  tabs = []
  totalColumns
  totalRows

  constructor(id, uiConfiguration, parent) {
    super(id, parent)
    this._uiConfiguration = uiConfiguration
    this.tabs = []
    this.items = []
  }

  fillFromJSON(jsonObj) {
    super.fillFromJSON(jsonObj)
    this.tabs = TabContainers.createTabsFromJSON({ json: jsonObj.tabs, parent: this })
    this.items = TabContainers.createItemsFromJSON({ json: jsonObj.children, parent: this })
  }

  get uiConfiguration() {
    return this._uiConfiguration
  }

  get survey() {
    return this.uiConfiguration.survey
  }

  isInVersion(version) {
    const innerTabsInVersion = this.tabs.filter((tab) => tab.isInVersion(version))
    const itemsInVersion = this.items.filter((item) => item.isInVersion(version))
    return innerTabsInVersion.length > 0 || itemsInVersion.length > 0
  }
}
