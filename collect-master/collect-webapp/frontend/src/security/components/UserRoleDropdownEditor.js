import React, { Component } from 'react';

export default class UserRoleDropdownEditor extends Component {
    
    constructor(props) {
        super(props)
        this.updateData = this.updateData.bind(this)
        this.handleChange = this.handleChange.bind(this)
        this.handleCancel = this.handleCancel.bind(this)

        this.state = { role: props.defaultValue }
    }

    focus() {
        this.refs.roleSelect.focus()
    }

    updateData() {
        this.props.onUpdate(this.state.role)
    }

    handleCancel() {
        this.setState({ role: this.props.defaultValue })
    }

    handleChange(e) {
        const role = e.currentTarget.value
        this.setState({ role: role })
        this.props.onUpdate(role)
    }

    render() {
        return (
            <span>
                <select
                    ref="roleSelect"
                    value={this.state.role}
                    onKeyDown={this.props.onKeyDown}
                    onChange={this.handleChange} >
                    {this.props.roles.map(role => (<option key={role} value={role}>{role}</option>))}
                </select>
            </span>
        );
    }
}