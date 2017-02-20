ui.routes.push({path: "user/profile", name: "profile", callback:
	function() {

    	this.breadCrumbView.collection.reset();
    	this.breadCrumbView.collection.add([
             {text:$.t('admin.home'), href:""},
             {text:$.t('admin.user-profile.show'), href:"manage/#user/profile"}
        ]);
    
        this.updateSidebar('user/profile');
        
    	var view = new UserProfileView({model: getUserInfo()});
    	$('#content').html(view.render().el);
    	
    	setPageTitle($.t('admin.user-profile.show'));
    	
    }
});