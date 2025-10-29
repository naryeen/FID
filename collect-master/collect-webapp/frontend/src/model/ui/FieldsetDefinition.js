import { TabContainers } from './TabContainers'
import { UIModelObjectDefinition } from './UIModelObjectDefinition'

export class FieldsetDefinition extends UIModelObjectDefinition {
  items = []
  tabs = []
  entityDefinitionId
  label
  column
  columnSpan
  row
  totalColumns
  totalRows

  constructor(id, parent) {
    super(id, parent)
    this.items = []
    this.tabs = []
  }

  fillFromJSON(jsonObj) {
    super.fillFromJSON(jsonObj)
    this.tabs = TabContainers.createTabsFromJSON({ json: jsonObj.tabs, parent: this })
    this.items = TabContainers.createItemsFromJSON({ json: jsonObj.children, parent: this })
  }

  get entityDefinition() {
    return this.parent.survey.schema.getDefinitionById(this.entityDefinitionId)
  }

  get nodeDefinition() {
    return this.entityDefinition
  }

  get nodeDefinitionId() {
    return this.entityDefinitionId
  }
}
