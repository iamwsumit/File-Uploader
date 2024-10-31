<?php

if ($_SERVER['REQUEST_METHOD'] == 'POST') {

    $target_dir = "upload/";
    $target_file = $target_dir . basename($_FILES["fileToUpload"]["name"]);

    header("Content-Type: application/json");

    if (!file_exists($target_dir)) {
        mkdir($target_dir, 0777, true);
    }

    if (file_exists($target_file)) {
        showResult(false, "File with this name already exist");
    } else {
        if (move_uploaded_file($_FILES["fileToUpload"]["tmp_name"], $target_file)) {
            $actual_link = "https://$_SERVER[HTTP_HOST]$_SERVER[REQUEST_URI]";
            $actual_link = str_replace(basename($actual_link), $target_file, $actual_link);
            showResult(true, $actual_link);
            echo "The file " . htmlspecialchars(basename($_FILES["fileToUpload"]["name"])) . " has been uploaded.";
        } else {
            showResult(false, "Something went wrong");
        }
    }
}

function showResult($success, $message)
{
    $myObj = array(
        "success" => $success,
        "result" => $message,
    );
    $myJSON = json_encode($myObj);
    echo $myJSON;
    die();
}
