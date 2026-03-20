<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Catalog Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 24px; }
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ccc; padding: 8px; text-align: left; }
        th { background: #f3f3f3; }
    </style>
</head>
<body>
<h1>Bibliography Catalog</h1>
<table>
    <thead>
    <tr>
        <th>ID</th>
        <th>Title</th>
        <th>Year</th>
        <th>Authors</th>
        <th>Location</th>
        <th>Concepts</th>
    </tr>
    </thead>
    <tbody>
    <#list resources as r>
        <tr>
            <td>${r.id}</td>
            <td>${r.title}</td>
            <td>${r.year}</td>
            <td>${r.authors}</td>
            <td><a href="${r.location}">${r.location}</a></td>
            <td>${r.concepts?join(", ")}</td>
        </tr>
    </#list>
    </tbody>
</table>
</body>
</html>
