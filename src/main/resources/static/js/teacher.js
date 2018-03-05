function addSubject() {
    //console.log("In Add");
    var name = document.getElementById("subnametxt").value;
    $.post("/addSubject", {name: name}, function (data, textStatus, jqXHR) {
        console.log(jqXHR.statusText);
        var responseMsg = jqXHR.statusText;
        if (responseMsg === "notmodified") {
            $("#modalMessage").text("Subject Already Exists");
            $("#successModal").modal("show");
        }
        else if (responseMsg === "nocontent") {
            $("#modalMessage").text("Subject Name cannot be Empty");
            $("#successModal").modal("show");

        }
        else {
            $("#modalMessage").text("Subject Added");
            $("#successModal").modal("show");
        }

        document.getElementById("subnametxt").value = "";
    });

}

function showSubjects() {
    $.getJSON("/teacher/subjects", function (data) {
        var sublist = "";
        console.log(data.name);
        for (var i = 0; i < data.length; i++) {
            sublist += "<li class=\"list-group-item\"><span>" + data[i].name + "</span></li>";
            console.log(data[i].id);
        }
        var listElement = $("#sub_list");
        listElement.empty();
        listElement.append(sublist);
        document.getElementById("sub_list").style.cursor = "pointer";
    });
}

function teachersub(e) {
    var subName = e.target.textContent;

    //console.log(subName);
    window.location = "/Teacher/Subject.html" + "?name=" + subName;
}

window.onload = function () {
    hideLogin();

    var btn = document.getElementById("addsubbtn");
    btn.addEventListener("click", function () {
        addSubject();
    });

    var btn1 = document.getElementById("tab_sub");
    btn1.addEventListener("click", function () {
        showSubjects();
    });

    var subList = document.getElementById("sub_list");
    subList.addEventListener("click", teachersub);


    /*  $.getJSON("/teacher/subjects", function (data) {
          var subId = data.id;
          console.log(subId);
      });*/


};