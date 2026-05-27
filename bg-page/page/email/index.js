$(function() {
  var authCode = getQueryString("authCode");
  
  ajax.get("/register/activate", {
    // data: $(form).serialize(),
    data: { authCode: authCode },
    success: function(resp) {
      if (resp.ok == false) {
        layer.alert(resp.msg);
      } else if (resp.ok) {
		window.location.href = "/tioim/login";
      }
    }
  });
});
