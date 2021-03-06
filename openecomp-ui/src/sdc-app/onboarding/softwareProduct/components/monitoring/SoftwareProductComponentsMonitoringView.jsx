import React, {Component, PropTypes} from 'react';
import Dropzone from 'react-dropzone';
import ButtonGroup from 'react-bootstrap/lib/ButtonGroup.js';
import ButtonToolbar from 'react-bootstrap/lib/ButtonToolbar.js';
import Button from 'react-bootstrap/lib/Button.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import SoftwareProductComponentsMonitoringConstants from './SoftwareProductComponentsMonitoringConstants.js';

class SoftwareProductComponentsMonitoringView extends Component {

	static propTypes = {
		isReadOnlyMode: PropTypes.bool,
		trapFilename: PropTypes.string,
		pollFilename: PropTypes.string,
		softwareProductId: PropTypes.string,

		onDropMibFileToUpload: PropTypes.func,
		onDeleteSnmpFile: PropTypes.func
	};

	state = {
		dragging: false
	};


	render() {
		return (
			<div className='vsp-component-monitoring'>
				{this.renderDropzoneWithType(SoftwareProductComponentsMonitoringConstants.SNMP_TRAP)}
				{this.renderDropzoneWithType(SoftwareProductComponentsMonitoringConstants.SNMP_POLL)}
			</div>
		);
	}

	renderDropzoneWithType(type) {
		let {isReadOnlyMode, trapFilename, pollFilename} = this.props;
		let fileName;
		if (type === SoftwareProductComponentsMonitoringConstants.SNMP_TRAP) {
			fileName = trapFilename;
		}
		else {
			fileName = pollFilename;
		}
		let refAndName = `fileInput${type.toString()}`;
		let typeDisplayName = this.getFileTypeDisplayName(type);
		return (
			<Dropzone
				className={`snmp-dropzone ${this.state.dragging ? 'active-dragging' : ''}`}
				onDrop={files => this.handleImport(files, {isReadOnlyMode, type, refAndName})}
				onDragEnter={() => this.handleOnDragEnter(isReadOnlyMode)}
				onDragLeave={() => this.setState({dragging:false})}
				multiple={false}
				disableClick={true}
				ref={refAndName}
				name={refAndName}
				accept='.zip'
				disabled>
				<div className='draggable-wrapper'>
					<div className='section-title'>{typeDisplayName}</div>
					{fileName ? this.renderUploadedFileName(fileName, type) : this.renderUploadButton(refAndName)}
				</div>
			</Dropzone>
		);
	}

	renderUploadButton(refAndName) {
		let {isReadOnlyMode} = this.props;
		return (
			<div
				className={`software-product-landing-view-top-block-col-upl${isReadOnlyMode ? ' disabled' : ''}`}>
				<div className='drag-text'>{i18n('Drag & drop for upload')}</div>
				<div className='or-text'>{i18n('or')}</div>
				<div className='upload-btn primary-btn' onClick={() => this.refs[refAndName].open()}>
					<span className='primary-btn-text'>{i18n('Select file')}</span>
				</div>
			</div>
		);
	}

	renderUploadedFileName(filename, type) {
		return (
			<ButtonToolbar>
				<ButtonGroup>
					<Button disabled>{filename}</Button>
					<Button className='delete-button' onClick={()=>this.props.onDeleteSnmpFile(type)}>X</Button>
				</ButtonGroup>
			</ButtonToolbar>
		);
	}


	handleOnDragEnter(isReadOnlyMode) {
		if (!isReadOnlyMode) {
			this.setState({dragging: true});
		}
	}

	handleImport(files, {isReadOnlyMode, type, refAndName}) {
		if (isReadOnlyMode) {
			return;
		}

		this.setState({dragging: false});
		let file = files[0];
		let formData = new FormData();
		formData.append('upload', file);
		this.refs[refAndName].value = '';
		this.props.onDropMibFileToUpload(formData, type);
	}

	getFileTypeDisplayName(type) {
		return type === SoftwareProductComponentsMonitoringConstants.SNMP_TRAP ? 'SNMP Trap' : 'SNMP Poll';
	}

}

export default SoftwareProductComponentsMonitoringView;
