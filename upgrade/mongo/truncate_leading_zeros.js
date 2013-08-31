db.phones.find({$where:"this._id.length > 10"}).forEach(function (e) {
    var old = doc._id; 
    doc._id = old.substring(1, 11); 
    db.phones.insert(doc); 
    db.phones.remove({_id : old});

    // TODO db.phones.find() removed in related
  }
)