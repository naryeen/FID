import React, { Component } from 'react';

import Constants from 'Constants';
import MaxAvailableSpaceContainer from 'common/components/MaxAvailableSpaceContainer';

class DataCleansingPage extends Component {
  render() {
	  return (
	    <MaxAvailableSpaceContainer>
	      <iframe src={Constants.BASE_URL + 'datacleansing/main.html'} 
					title="Open Foris Collect - Data Cleansing Toolkit"
					width="100%" height="100%" />
	    </MaxAvailableSpaceContainer>
	  );
  }
}

export default DataCleansingPage;