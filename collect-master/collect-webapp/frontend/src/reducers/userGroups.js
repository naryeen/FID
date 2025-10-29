import update from 'immutability-helper'
import Arrays from 'utils/Arrays'

import {
  REQUEST_USER_GROUPS,
  RECEIVE_USER_GROUPS,
  RECEIVE_USER_GROUP,
  INVALIDATE_USER_GROUPS,
  USER_GROUPS_DELETED,
} from 'actions/usergroups'

function userGroups(
  state = {
    initialized: false,
    isFetching: false,
    didInvalidate: false,
    items: [],
  },
  action
) {
  switch (action.type) {
    case INVALIDATE_USER_GROUPS:
      return Object.assign({}, state, {
        didInvalidate: true,
      })
    case REQUEST_USER_GROUPS:
      return Object.assign({}, state, {
        isFetching: true,
        didInvalidate: false,
      })
    case RECEIVE_USER_GROUPS:
      action.userGroups.forEach((ug) => {
        //adjust parent group reference
        ug.parent = ug.parentId ? action.userGroups.find((ug2) => ug2.id === ug.parentId) : null
        ug.children = ug.childrenGroupIds
          .map((id) => action.userGroups.find((ug2) => ug2.id === id))
          .filter((ug) => ug !== undefined)
      })

      return Object.assign({}, state, {
        initialized: true,
        isFetching: false,
        didInvalidate: false,
        items: action.userGroups,
        lastUpdated: action.receivedAt,
      })
    case RECEIVE_USER_GROUP:
      const newUserGroup = action.userGroup
      const userGroups = state.items
      const oldUserGroupIdx = userGroups.findIndex((u) => u.id === newUserGroup.id)

      if (oldUserGroupIdx >= 0) {
        const oldUserGroup = userGroups[oldUserGroupIdx]
        const oldUserGroupParentId = oldUserGroup.parentId
        if (oldUserGroupParentId !== null) {
          const oldParentGroup = userGroups.find((ug) => ug.id === oldUserGroupParentId)
          oldParentGroup.childrenGroupIds = Arrays.removeItem(oldParentGroup.childrenGroupIds, newUserGroup.id)
          oldParentGroup.children = Arrays.removeItem(oldParentGroup.children, oldUserGroup)
        }
      }

      if (newUserGroup.parentId === null) {
        newUserGroup.parent = null
      } else {
        const parentGroup = userGroups.find((ug) => ug.id === newUserGroup.parentId)
        newUserGroup.parent = parentGroup
        if (parentGroup.childrenGroupIds.indexOf(newUserGroup.id) < 0) {
          parentGroup.childrenGroupIds.push(newUserGroup.id)
          parentGroup.children.push(newUserGroup)
        }
      }

      let newUserGroups
      if (oldUserGroupIdx >= 0) {
        newUserGroups = update(userGroups, {
          $splice: [[oldUserGroupIdx, 1, newUserGroup]],
        })
      } else {
        newUserGroups = Arrays.addItem(userGroups, newUserGroup)
      }
      return Object.assign({}, state, {
        items: newUserGroups,
        lastUpdated: action.receivedAt,
      })
    case USER_GROUPS_DELETED: {
      const deletedIds = action.itemIds
      const deletedItems = deletedIds.map((id) => state.items.find((item) => item.id === id))
      const newItems = Arrays.removeItems(state.items, deletedItems)
      return Object.assign({}, state, {
        items: newItems,
        lastUpdated: action.receivedAt,
      })
    }
    default:
      return state
  }
}

export default userGroups
